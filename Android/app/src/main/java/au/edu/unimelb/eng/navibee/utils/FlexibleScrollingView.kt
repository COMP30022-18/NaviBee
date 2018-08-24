package au.edu.unimelb.eng.navibee.utils

import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.RecyclerView
import android.view.View

class FlexibleScrollingView(): AppBarLayout.ScrollingViewBehavior() {
    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is RecyclerView
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return super.onDependentViewChanged(parent, child, dependency)
    }
}