package com.udacity.project4.androidTest.util

import android.content.res.Resources
import android.view.View
import androidx.core.util.Preconditions
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/**
 *  Copyright 2018 Danny Roa

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

class RecyclerViewMatcher(private val recyclerId: Int) {
    fun atPosition(position: Int): TypeSafeMatcher<View?> {
        return atPositionOnView(position, UNSPECIFIED)
    }

    fun atPositionOnView(position: Int, targetViewId: Int): TypeSafeMatcher<View?> {
        return object : TypeSafeMatcher<View?>() {
            var resources: Resources? = null
            var recycler: RecyclerView? = null
            var holder: RecyclerView.ViewHolder? = null
            override fun describeTo(description: Description) {
                Preconditions.checkState(
                    resources != null,
                    "resource should be init by matchesSafely()"
                )
                if (recycler == null) {
                    description.appendText("RecyclerView with " + getResourceName(recyclerId))
                    return
                }
                if (holder == null) {
                    description.appendText(
                        String.format(
                            "in RecyclerView (%s) at position %s",
                            getResourceName(recyclerId), position
                        )
                    )
                    return
                }
                if (targetViewId == UNSPECIFIED) {
                    description.appendText(
                        String.format(
                            "in RecyclerView (%s) at position %s",
                            getResourceName(recyclerId), position
                        )
                    )
                    return
                }
                description.appendText(
                    String.format(
                        "in RecyclerView (%s) at position %s and with %s",
                        getResourceName(recyclerId),
                        position,
                        getResourceName(targetViewId)
                    )
                )
            }

            private fun getResourceName(id: Int): String {
                return try {
                    "R.id." + (resources?.getResourceEntryName(id) ?: "")
                } catch (ex: Resources.NotFoundException) {
                    String.format("resource id %s - name not found", id)
                }
            }

            override fun matchesSafely(view: View?): Boolean {
                if (view != null) {
                    resources = view.resources
                }
                if (view != null) {
                    recycler = view.rootView.findViewById(recyclerId)
                }
                if (recycler == null) return false
                holder = recycler!!.findViewHolderForAdapterPosition(position)
                if (holder == null) return false
                return if (targetViewId == UNSPECIFIED) {
                    view === holder!!.itemView
                } else {
                    view === holder!!.itemView.findViewById<View>(targetViewId)
                }
            }
        }
    }

    companion object {
        const val UNSPECIFIED = -1
    }
}
