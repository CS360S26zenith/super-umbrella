package com.example.campuseventstest.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Explore — search, category buckets, date/price/sort filters, rich event cards.
 */
public class StudentExploreFragment extends Fragment {

    private enum Bucket {
        ALL,
        ACADEMIC,
        SPORTS,
        CULTURAL
    }

    private enum SortMode {
        DATE_ASC,
        DATE_DESC,
        TITLE_ASC
    }

    private EditText searchBar;
    private ChipGroup categoryChips;
    private View filterDateRow;
    private View filterPriceRow;
    private View filterSortRow;
    private TextView filterDateValue;
    private TextView filterPriceValue;
    private TextView filterSortValue;
    private TextView resultsCountView;
    private TextView emptyText;
    private ProgressBar loadingBar;
    private RecyclerView recyclerView;
    private ExploreEventAdapter adapter;

    private FirestoreService firestoreService;
    private List<Event> allLiveEvents = new ArrayList<>();

    private Bucket bucket = Bucket.ALL;
    private String searchQuery = "";
    private Calendar filterStartDay;
    private Calendar filterEndDay;
    private int priceMinPkr = 0;
    private int priceMaxPkr = 500_000;
    private SortMode sortMode = SortMode.DATE_DESC;

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private static final long SEARCH_DEBOUNCE_MS = 200L;

    private long lastLoadElapsed;
    private static final long MIN_RELOAD_MS = 2000L;
    private boolean loadInFlight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreService = new FirestoreService();

        searchBar = view.findViewById(R.id.explore_search_bar);
        categoryChips = view.findViewById(R.id.explore_category_chips);
        filterDateRow = view.findViewById(R.id.filter_date_row);
        filterPriceRow = view.findViewById(R.id.filter_price_row);
        filterSortRow = view.findViewById(R.id.filter_sort_row);
        filterDateValue = view.findViewById(R.id.filter_date_value);
        filterPriceValue = view.findViewById(R.id.filter_price_value);
        filterSortValue = view.findViewById(R.id.filter_sort_value);
        resultsCountView = view.findViewById(R.id.explore_results_count);
        emptyText = view.findViewById(R.id.explore_empty_text);
        loadingBar = view.findViewById(R.id.explore_loading_bar);
        recyclerView = view.findViewById(R.id.explore_recycler);

        adapter = new ExploreEventAdapter(requireContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        categoryChips.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) {
                return;
            }
            int id = ids.get(0);
            if (id == R.id.chip_explore_all) {
                bucket = Bucket.ALL;
            } else if (id == R.id.chip_explore_academic) {
                bucket = Bucket.ACADEMIC;
            } else if (id == R.id.chip_explore_sports) {
                bucket = Bucket.SPORTS;
            } else if (id == R.id.chip_explore_cultural) {
                bucket = Bucket.CULTURAL;
            }
            applyFiltersAndRender();
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString() : "";
                if (debounceRunnable != null) {
                    debounceHandler.removeCallbacks(debounceRunnable);
                }
                debounceRunnable = StudentExploreFragment.this::applyFiltersAndRender;
                debounceHandler.postDelayed(debounceRunnable, SEARCH_DEBOUNCE_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        filterDateRow.setOnClickListener(v -> showDateRangeDialog());
        filterPriceRow.setOnClickListener(v -> showPriceRangeDialog());
        filterSortRow.setOnClickListener(v -> showSortDialog());

        view.findViewById(R.id.link_societies).setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), SocietiesListActivity.class)));
        view.findViewById(R.id.link_calendar).setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), MyCalendarActivity.class)));
        view.findViewById(R.id.link_notifications).setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), NotificationsActivity.class)));
        view.findViewById(R.id.link_help).setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), HelpAssistantActivity.class)));

        refreshFilterLabels();
        loadEvents();
    }

    private void loadEvents() {
        if (loadInFlight) {
            return;
        }
        loadInFlight = true;
        loadingBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        firestoreService.getLiveEvents(new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                loadInFlight = false;
                loadingBar.setVisibility(View.GONE);
                lastLoadElapsed = SystemClock.elapsedRealtime();
                allLiveEvents = events != null ? events : new ArrayList<>();
                applyFiltersAndRender();
            }

            @Override
            public void onFailure(String error) {
                loadInFlight = false;
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean passesBucket(Event e) {
        String cat = e.getCategory();
        switch (bucket) {
            case ALL:
                return true;
            case ACADEMIC:
                return Constants.CATEGORY_TALKS.equalsIgnoreCase(cat);
            case SPORTS:
                return Constants.CATEGORY_SPORTS.equalsIgnoreCase(cat);
            case CULTURAL:
                return Constants.CATEGORY_CLUBS.equalsIgnoreCase(cat)
                        || Constants.CATEGORY_PERFORMANCES.equalsIgnoreCase(cat);
            default:
                return true;
        }
    }

    private boolean passesSearch(Event e) {
        if (searchQuery.trim().isEmpty()) {
            return true;
        }
        String q = searchQuery.toLowerCase(Locale.US);
        return (e.getTitle() != null && e.getTitle().toLowerCase(Locale.US).contains(q))
                || (e.getDescription() != null && e.getDescription().toLowerCase(Locale.US).contains(q))
                || (e.getVenue() != null && e.getVenue().toLowerCase(Locale.US).contains(q))
                || (e.getSocietyName() != null && e.getSocietyName().toLowerCase(Locale.US).contains(q));
    }

    private boolean passesDate(Event e) {
        if (filterStartDay == null && filterEndDay == null) {
            return true;
        }
        if (e.getDate() == null) {
            return false;
        }
        long t = e.getDate().toDate().getTime();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(t);
        stripTime(c);
        long eventDay = c.getTimeInMillis();

        if (filterStartDay != null && filterEndDay != null) {
            long start = filterStartDay.getTimeInMillis();
            long end = endOfDay(filterEndDay);
            return eventDay >= start && t <= end;
        }
        if (filterStartDay != null) {
            return eventDay >= filterStartDay.getTimeInMillis();
        }
        if (filterEndDay != null) {
            return t <= endOfDay(filterEndDay);
        }
        return true;
    }

    private static long endOfDay(Calendar day) {
        Calendar c = (Calendar) day.clone();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    private static void stripTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private boolean passesPrice(Event e) {
        double p = e.getEffectiveTicketPricePkr();
        return p >= priceMinPkr && p <= priceMaxPkr;
    }

    private void applyFiltersAndRender() {
        List<Event> out = new ArrayList<>();
        for (Event e : allLiveEvents) {
            if (passesBucket(e) && passesSearch(e) && passesDate(e) && passesPrice(e)) {
                out.add(e);
            }
        }
        sortEvents(out);
        adapter.setEvents(out);
        resultsCountView.setText(getString(R.string.explore_results_fmt, out.size()));
        emptyText.setVisibility(out.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(out.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void sortEvents(List<Event> list) {
        Comparator<Event> cmp;
        switch (sortMode) {
            case DATE_DESC:
                cmp = Comparator.comparing((Event e) ->
                        e.getDate() != null ? e.getDate().toDate().getTime() : 0L).reversed();
                break;
            case TITLE_ASC:
                cmp = Comparator.comparing(e -> e.getTitle() != null ? e.getTitle().toLowerCase(Locale.US) : "");
                break;
            case DATE_ASC:
            default:
                cmp = Comparator.comparing((Event e) ->
                        e.getDate() != null ? e.getDate().toDate().getTime() : Long.MAX_VALUE);
                break;
        }
        Collections.sort(list, cmp);
    }

    private void refreshFilterLabels() {
        SimpleDateFormat df = new SimpleDateFormat("MMM d", Locale.getDefault());
        if (filterStartDay != null && filterEndDay != null) {
            filterDateValue.setText(df.format(filterStartDay.getTime()) + " – "
                    + df.format(filterEndDay.getTime()));
        } else if (filterStartDay != null) {
            filterDateValue.setText(getString(R.string.filter_from_fmt, df.format(filterStartDay.getTime())));
        } else if (filterEndDay != null) {
            filterDateValue.setText(getString(R.string.filter_until_fmt, df.format(filterEndDay.getTime())));
        } else {
            filterDateValue.setText(R.string.filter_any_date);
        }

        if (priceMinPkr <= 0 && priceMaxPkr >= 500_000) {
            filterPriceValue.setText(R.string.filter_price_any);
        } else {
            filterPriceValue.setText(getString(R.string.filter_price_fmt, priceMinPkr, priceMaxPkr));
        }

        switch (sortMode) {
            case DATE_DESC:
                filterSortValue.setText(R.string.sort_latest_date_desc);
                break;
            case TITLE_ASC:
                filterSortValue.setText(R.string.sort_title_az);
                break;
            case DATE_ASC:
            default:
                filterSortValue.setText(R.string.sort_soonest);
                break;
        }
    }

    private void showDateRangeDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_date_range)
                .setItems(new CharSequence[]{
                        getString(R.string.filter_any_date),
                        getString(R.string.pick_start_date),
                        getString(R.string.pick_end_date),
                        getString(R.string.clear_dates)
                }, (d, which) -> {
                    if (which == 0) {
                        filterStartDay = null;
                        filterEndDay = null;
                        refreshFilterLabels();
                        applyFiltersAndRender();
                    } else if (which == 1) {
                        pickDate(true);
                    } else if (which == 2) {
                        pickDate(false);
                    } else {
                        filterStartDay = null;
                        filterEndDay = null;
                        refreshFilterLabels();
                        applyFiltersAndRender();
                    }
                })
                .show();
    }

    private void pickDate(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, dayOfMonth);
                    stripTime(c);
                    if (isStart) {
                        filterStartDay = c;
                    } else {
                        filterEndDay = c;
                    }
                    refreshFilterLabels();
                    applyFiltersAndRender();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    private void showPriceRangeDialog() {
        final String[] options = {
                getString(R.string.price_preset_any),
                getString(R.string.price_preset_free),
                getString(R.string.price_preset_low),
                getString(R.string.price_preset_mid)
        };
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_price_range)
                .setItems(options, (d, which) -> {
                    switch (which) {
                        case 0:
                            priceMinPkr = 0;
                            priceMaxPkr = 500_000;
                            break;
                        case 1:
                            priceMinPkr = 0;
                            priceMaxPkr = 0;
                            break;
                        case 2:
                            priceMinPkr = 0;
                            priceMaxPkr = 500;
                            break;
                        case 3:
                            priceMinPkr = 0;
                            priceMaxPkr = 5000;
                            break;
                        default:
                            break;
                    }
                    refreshFilterLabels();
                    applyFiltersAndRender();
                })
                .show();
    }

    private void showSortDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_sort)
                .setItems(new CharSequence[]{
                        getString(R.string.sort_soonest),
                        getString(R.string.sort_latest_date_desc),
                        getString(R.string.sort_title_az)
                }, (d, which) -> {
                    if (which == 0) {
                        sortMode = SortMode.DATE_ASC;
                    } else if (which == 1) {
                        sortMode = SortMode.DATE_DESC;
                    } else {
                        sortMode = SortMode.TITLE_ASC;
                    }
                    refreshFilterLabels();
                    applyFiltersAndRender();
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        long now = SystemClock.elapsedRealtime();
        if (now - lastLoadElapsed > MIN_RELOAD_MS && !loadInFlight) {
            loadEvents();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
    }
}
