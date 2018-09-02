package au.edu.unimelb.eng.navibee.utils

import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class FlexibleScrollingView(): AppBarLayout.ScrollingViewBehavior() {
    override fun layoutDependsOn(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is androidx.recyclerview.widget.RecyclerView
    }

    override fun onDependentViewChanged(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: View, dependency: View): Boolean {
        return super.onDependentViewChanged(parent, child, dependency)
    }
}