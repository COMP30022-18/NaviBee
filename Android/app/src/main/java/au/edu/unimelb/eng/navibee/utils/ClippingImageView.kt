package au.edu.unimelb.eng.navibee.utils

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.ImageView

class ClippingImageView: ImageView {
    constructor(context: Context?) :
            super(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            clipToOutline = true
        }
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            clipToOutline = true
        }
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            clipToOutline = true
        }
    }

}