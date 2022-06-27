package com.pesdk.uisdk.fragment.canvas;

import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_SETTLING;

/**
 * @author JIAN
 * @create 2021/12/20
 * @Describe
 */
public class TabLayoutMediator {
    @NonNull
    private final TabLayout tabLayout;
    @NonNull
    private final ViewPager2 viewPager;
    private final boolean autoRefresh;
    private final boolean smoothScroll;
    private final TabConfigurationStrategy tabConfigurationStrategy;
    @Nullable
    private RecyclerView.Adapter<?> adapter;
    private boolean attached;
    private Callback mCallback;
    @Nullable
    private TabLayoutOnPageChangeCallback onPageChangeCallback;
    @Nullable
    private TabLayout.OnTabSelectedListener onTabSelectedListener;
    @Nullable
    private RecyclerView.AdapterDataObserver pagerAdapterObserver;

    /**
     * A callback interface that must be implemented to set the text and styling of newly created
     * tabs.
     */
    public interface TabConfigurationStrategy {
        /**
         * Called to configure the tab for the page at the specified position. Typically calls {@link
         * TabLayout.Tab#setText(CharSequence)}, but any form of styling can be applied.
         *
         * @param tab      The Tab which should be configured to represent the title of the item at the given
         *                 position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        void onConfigureTab(@NonNull TabLayout.Tab tab, int position);
    }

    public interface Callback {
        /**
         * 选中tab
         */
        void onTabSelected(TabLayout.Tab tab);
    }

    public TabLayoutMediator(
            @NonNull TabLayout tabLayout,
            @NonNull ViewPager2 viewPager,
            boolean autoRefresh,
            @NonNull TabConfigurationStrategy tabConfigurationStrategy, Callback callback) {
        this(tabLayout, viewPager, autoRefresh, /* smoothScroll= */ true, tabConfigurationStrategy, callback);
    }

    public TabLayoutMediator(
            @NonNull TabLayout tabLayout,
            @NonNull ViewPager2 viewPager,
            boolean autoRefresh,
            boolean smoothScroll,
            @NonNull TabConfigurationStrategy tabConfigurationStrategy, Callback callback) {
        this.tabLayout = tabLayout;
        this.viewPager = viewPager;
        this.autoRefresh = autoRefresh;
        this.smoothScroll = smoothScroll;
        this.tabConfigurationStrategy = tabConfigurationStrategy;
        this.mCallback = callback;
    }

    /**
     * Link the TabLayout and the ViewPager2 together. Must be called after ViewPager2 has an adapter
     * set. To be called on a new instance of TabLayoutMediator or if the ViewPager2's adapter
     * changes.
     *
     * @throws IllegalStateException If the mediator is already attached, or the ViewPager2 has no
     *                               adapter.
     */
    public void attach() {
        if (attached) {
            throw new IllegalStateException("TabLayoutMediator is already attached");
        }
        adapter = viewPager.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException(
                    "TabLayoutMediator attached before ViewPager2 has an " + "adapter");
        }
        attached = true;

        // Add our custom OnPageChangeCallback to the ViewPager
        onPageChangeCallback = new TabLayoutOnPageChangeCallback(tabLayout);
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);

        // Now we'll add a tab selected listener to set ViewPager's current item
        onTabSelectedListener = new ViewPagerOnTabSelectedListener(viewPager, smoothScroll, tabLayout);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        // Now we'll populate ourselves from the pager adapter, adding an observer if
        // autoRefresh is enabled
        if (autoRefresh) {
            // Register our observer on the new adapter
            pagerAdapterObserver = new PagerAdapterObserver();
            adapter.registerAdapterDataObserver(pagerAdapterObserver);
        }

        populateTabsFromPagerAdapter();

        // Now update the scroll position to match the ViewPager's current item
        tabLayout.setScrollPosition(1, 0f, true);
//        tabLayout.setScrollPosition(viewPager.getCurrentItem(), 0f, true);
    }

    /**
     * Unlink the TabLayout and the ViewPager. To be called on a stale TabLayoutMediator if a new one
     * is instantiated, to prevent holding on to a view that should be garbage collected. Also to be
     * called before {@link #attach()} when a ViewPager2's adapter is changed.
     */
    public void detach() {
        if (autoRefresh && adapter != null) {
            adapter.unregisterAdapterDataObserver(pagerAdapterObserver);
            pagerAdapterObserver = null;
        }
        tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
        onTabSelectedListener = null;
        onPageChangeCallback = null;
        adapter = null;
        attached = false;
    }

    /**
     * Returns whether the {@link TabLayout} and the {@link ViewPager2} are linked together.
     */
    public boolean isAttached() {
        return attached;
    }

    @SuppressWarnings("WeakerAccess")
    void populateTabsFromPagerAdapter() {
//        tabLayout.removeAllTabs();
//
        if (adapter != null) {
//            int adapterCount = adapter.getItemCount();
//            for (int i = 0; i < adapterCount; i++) {
//                TabLayout.Tab tab = tabLayout.newTab();
//                tabConfigurationStrategy.onConfigureTab(tab, i);
//                tabLayout.addTab(tab, false);
//            }
            // Make sure we reflect the currently set ViewPager item
//            if (adapterCount > 0) {
//                int lastItem = tabLayout.getTabCount() - 1;
//                int currItem = Math.min(viewPager.getCurrentItem(), lastItem);
//                if (currItem != tabLayout.getSelectedTabPosition()) {
//                    tabLayout.selectTab(tabLayout.getTabAt(currItem));
//                }
//            }
        }
    }

    /**
     * A {@link ViewPager2.OnPageChangeCallback} class which contains the necessary calls back to the
     * provided {@link TabLayout} so that the tab position is kept in sync.
     *
     * <p>This class stores the provided TabLayout weakly, meaning that you can use {@link
     * ViewPager2#registerOnPageChangeCallback(ViewPager2.OnPageChangeCallback)} without removing the
     * callback and not cause a leak.
     */
    private static class TabLayoutOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
        @NonNull
        private final WeakReference<TabLayout> tabLayoutRef;
        private int previousScrollState;
        private int scrollState;

        TabLayoutOnPageChangeCallback(TabLayout tabLayout) {
            tabLayoutRef = new WeakReference<>(tabLayout);
            reset();
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            previousScrollState = scrollState;
            scrollState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            TabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null) {
                // Only update the text selection if we're not settling, or we are settling after
                // being dragged
                boolean updateText =
                        scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING;
                // Update the indicator if we're not settling after being idle. This is caused
                // from a setCurrentItem() call and will be handled by an animation from
                // onPageSelected() instead.
                boolean updateIndicator =
                        !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
                tabLayout.setScrollPosition(position+1, positionOffset, updateText, updateIndicator);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            TabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null
                    && tabLayout.getSelectedTabPosition() != (position + 1)
                    && position < tabLayout.getTabCount()) {
                boolean updateIndicator =
                        scrollState == SCROLL_STATE_IDLE
                                || (scrollState == SCROLL_STATE_SETTLING
                                && previousScrollState == SCROLL_STATE_IDLE);
                try {
                    TabLayout.Tab tab = tabLayout.getTabAt(position + 1);
                    tabLayout.selectTab(tab, updateIndicator);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void reset() {
            previousScrollState = scrollState = SCROLL_STATE_IDLE;
        }
    }

    private static final String TAG = "TabLayoutMediator";

    /**
     * A {@link TabLayout.OnTabSelectedListener} class which contains the necessary calls back to the
     * provided {@link ViewPager2} so that the tab position is kept in sync.
     */
    private class ViewPagerOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
        private final ViewPager2 viewPager;
        private final boolean smoothScroll;
        @NonNull
        private final WeakReference<TabLayout> tabLayoutRef;

        ViewPagerOnTabSelectedListener(ViewPager2 viewPager, boolean smoothScroll, TabLayout tabLayout) {
            this.viewPager = viewPager;
            this.smoothScroll = smoothScroll;
            tabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onTabSelected(@NonNull TabLayout.Tab tab) {
            if (tab.getPosition() >= 1) {
                viewPager.setCurrentItem(tab.getPosition() - 1, smoothScroll);
            } else {
                TabLayout tmp = tabLayoutRef.get();
                if (null != tmp) {
                    tmp.setScrollPosition(0, 0, true, true);
                }
            }
            mCallback.onTabSelected(tab);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            // No-op
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            // No-op
        }
    }

    private class PagerAdapterObserver extends RecyclerView.AdapterDataObserver {
        PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            populateTabsFromPagerAdapter();
        }
    }
}
