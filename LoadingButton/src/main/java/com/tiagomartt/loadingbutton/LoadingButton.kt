package com.tiagomartt.loadingbutton

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.tiagomartt.loadingbutton.databinding.LoadingButtonBinding
import java.io.Serializable

@SuppressLint("ClickableViewAccessibility")
class LoadingButton @JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var rippleColor = 0
    private var _backgroundColor = 0
    private var backgroundColorDisabled = 0
    private var strokeColor = 0
    private var strokeColorDisabled = 0
    private var strokeWidth = 0f
    private var cornerRadius = 0f

    private var progressBarColor = 0

    private var androidText = ""
    private var androidTextColor = 0
    private var androidTextSizeUnit = 0
    private var androidTextSize = 0f
    private var androidFontFamilyResId = 0
    private var androidTextAlignment = 0
    private var androidTextAllCaps = false

    private var imageSrcId = 0
    private var imageTint = 0
    private var imageScaleType = 3
    private var imageViewScaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER

    private var loadingButtonState: LoadingButtonState = LoadingButtonState.Normal
        set(value) {
            field = value
            refreshState()
        }

    private val binding = LoadingButtonBinding.inflate(LayoutInflater.from(context), this, true)

    private lateinit var enabledShape: GradientDrawable
    private lateinit var disabledShape: GradientDrawable
    private lateinit var stateListDrawable: StateListDrawable
    private lateinit var rippleDrawable: RippleDrawable

    init {

        isSaveEnabled = true

        attrs?.let {

            val attributes = context.obtainStyledAttributes(it, R.styleable.LoadingButton)

            rippleColor = attributes.getColor(R.styleable.LoadingButton_rippleColor, ContextCompat.getColor(context, R.color.defaultRippleColor))

            _backgroundColor = attributes.getColor(R.styleable.LoadingButton_backgroundColor, ContextCompat.getColor(context, R.color.defaultBackgroundColorEnabled))

            backgroundColorDisabled = _backgroundColor

            strokeColor = attributes.getColor(R.styleable.LoadingButton_strokeColor, ContextCompat.getColor(context, R.color.defaultStrokeColorEnabled))

            strokeColorDisabled = strokeColor

            strokeWidth  = attributes.getDimension(R.styleable.LoadingButton_strokeWidth, context.resources.getDimension(R.dimen.defaultStrokeWidth))

            cornerRadius = attributes.getDimension(R.styleable.LoadingButton_cornerRadius, context.resources.getDimension(R.dimen.defaultCornerRadius))

            progressBarColor = attributes.getColor(R.styleable.LoadingButton_progressBarColor, ContextCompat.getColor(context, R.color.defaultProgressBarColor))

            androidText = attributes.getText(R.styleable.LoadingButton_android_text)?.toString() ?: ""

            androidTextColor = attributes.getColor(R.styleable.LoadingButton_android_textColor, ContextCompat.getColor(context, R.color.defaultTextColor))

            androidTextSizeUnit = TypedValue.COMPLEX_UNIT_PX

            androidTextSize = attributes.getDimension(R.styleable.LoadingButton_android_textSize, context.resources.getDimension(R.dimen.defaultFontSize))

            androidFontFamilyResId = attributes.getResourceId(R.styleable.LoadingButton_android_fontFamily, 0)

            androidTextAlignment = attributes.getInt(R.styleable.LoadingButton_android_textAlignment, View.TEXT_ALIGNMENT_CENTER)

            androidTextAllCaps = attributes.getBoolean(R.styleable.LoadingButton_android_textAllCaps, true)

            imageScaleType = attributes.getInt(R.styleable.LoadingButton_imageScaleType, 3)

            when (imageScaleType) {
                0 -> {
                    imageViewScaleType = ImageView.ScaleType.MATRIX
                }
                1 -> {
                    imageViewScaleType = ImageView.ScaleType.FIT_XY
                }
                2 -> {
                    imageViewScaleType = ImageView.ScaleType.FIT_START
                }
                3 -> {
                    imageViewScaleType = ImageView.ScaleType.FIT_CENTER
                }
                4 -> {
                    imageViewScaleType = ImageView.ScaleType.FIT_END
                }
                5 -> {
                    imageViewScaleType = ImageView.ScaleType.CENTER
                }
                6 -> {
                    imageViewScaleType = ImageView.ScaleType.CENTER_CROP
                }
                7 -> {
                    imageViewScaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            }

            imageSrcId = attributes.getResourceId(R.styleable.LoadingButton_imageSrc, 0)

            imageTint = attributes.getColor(R.styleable.LoadingButton_imageTint, 0)

            attributes.recycle()
        }

        updateLayout()

        refreshState()
    }

    private fun updateLayout() {

        enabledShape = GradientDrawable()
        enabledShape.shape = GradientDrawable.RECTANGLE
        enabledShape.cornerRadius = cornerRadius
        enabledShape.setColor(_backgroundColor)
        enabledShape.setStroke(strokeWidth.toInt(), strokeColor)

        disabledShape = GradientDrawable()
        disabledShape.shape = GradientDrawable.RECTANGLE
        disabledShape.cornerRadius = cornerRadius
        disabledShape.setColor(backgroundColorDisabled)
        disabledShape.setStroke(strokeWidth.toInt(), strokeColorDisabled)

        stateListDrawable = StateListDrawable()
        stateListDrawable.setExitFadeDuration(100)
        stateListDrawable.setEnterFadeDuration(50)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_enabled), enabledShape)
        stateListDrawable.addState(intArrayOf(-android.R.attr.state_enabled), disabledShape)

        rippleDrawable = RippleDrawable(ColorStateList.valueOf(rippleColor), stateListDrawable, null)

        background = rippleDrawable

        binding.progressBar.indeterminateTintList = ColorStateList.valueOf(progressBarColor)

        binding.progressBar.bringToFront()

        binding.textView.text = androidText

        binding.textView.setTextColor(androidTextColor)

        binding.textView.setTextSize(androidTextSizeUnit, androidTextSize)

        if (androidFontFamilyResId != 0) binding.textView.typeface = ResourcesCompat.getFont(context, androidFontFamilyResId)

        binding.textView.textAlignment = androidTextAlignment

        binding.textView.isAllCaps = androidTextAllCaps

        binding.imageView.scaleType = imageViewScaleType

        if (imageSrcId != 0) binding.imageView.setImageResource(imageSrcId)

        if (imageTint != 0) binding.imageView.imageTintList = ColorStateList.valueOf(imageTint)
    }

    private fun refreshState() {

        isEnabled = loadingButtonState.isEnabled
        isFocusable = loadingButtonState.isEnabled
        isClickable = loadingButtonState.isEnabled

        refreshDrawableState()

        binding.progressBar.visibility = loadingButtonState.progressBarVisibility

        binding.textView.visibility = loadingButtonState.textViewVisibility

        binding.imageView.visibility = loadingButtonState.imageViewVisibility

        if (loadingButtonState == LoadingButtonState.Loading) {

            val va = ValueAnimator.ofFloat(alpha, 0.75f)
            va.duration = 100
            va.addUpdateListener { animation -> alpha = animation.animatedValue as Float }
            va.start()

        } else if (loadingButtonState == LoadingButtonState.Normal) {

            val va = ValueAnimator.ofFloat(alpha, 1.0f)
            va.duration = 100
            va.addUpdateListener { animation -> alpha = animation.animatedValue as Float }
            va.start()
        }
    }

    fun setLoading(l: Boolean) {
        loadingButtonState = if (l) {
            LoadingButtonState.Loading
        } else {
            LoadingButtonState.Normal
        }
    }

    fun setRippleColor(@ColorInt rippleColor: Int) {
        this.rippleColor = rippleColor
        rippleDrawable.setColor(ColorStateList.valueOf(rippleColor))
    }

    fun setBackgroundColorEnabled(@ColorInt backgroundColorEnabled: Int) {
        this._backgroundColor = backgroundColorEnabled
        enabledShape.setColor(backgroundColorEnabled)
    }

    fun setBackgroundColorDisabled(@ColorInt backgroundColorDisabled: Int) {
        this.backgroundColorDisabled = backgroundColorDisabled
        disabledShape.setColor(backgroundColorDisabled)
    }

    fun setStrokeColorEnabled(@ColorInt strokeColorEnabled: Int) {
        this.strokeColor = strokeColorEnabled
        enabledShape.setStroke(this.strokeWidth.toInt(), strokeColorEnabled)
    }

    fun setStrokeColorDisabled(@ColorInt strokeColorDisabled: Int) {
        this.strokeColorDisabled = strokeColorDisabled
        disabledShape.setStroke(this.strokeWidth.toInt(), strokeColorDisabled)
    }

    fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = pxFromDp(context, strokeWidth)
        enabledShape.setStroke(this.strokeWidth.toInt(), strokeColor)
        disabledShape.setStroke(this.strokeWidth.toInt(), strokeColorDisabled)
    }

    fun setCornerRadius(cornerRadius: Float) {
        this.cornerRadius = pxFromDp(context, cornerRadius)
        enabledShape.cornerRadius = this.cornerRadius
        disabledShape.cornerRadius = this.cornerRadius
    }

    fun setProgressBarColor(@ColorInt progressBarColor: Int) {
        this.progressBarColor = progressBarColor
        binding.progressBar.indeterminateTintList = ColorStateList.valueOf(progressBarColor)
    }

    fun setText(text: String) {

        this.androidText = text

        binding.textView.text = this.androidText
    }

    fun setText(@StringRes resId: Int) {

        this.androidText = context.getString(resId)

        binding.textView.text = this.androidText
    }

    fun setTextColor(@ColorInt textColor: Int) {
        this.androidTextColor = textColor
        binding.textView.setTextColor(androidTextColor)
    }

    fun setTextSize(unit: Int, textSize: Float) {
        this.androidTextSize = textSize
        this.androidTextSizeUnit = unit
        binding.textView.setTextSize(androidTextSizeUnit, androidTextSize)
    }

    fun setTextSize(textSize: Float) {
        this.androidTextSize = textSize
        this.androidTextSizeUnit = TypedValue.COMPLEX_UNIT_SP
        binding.textView.setTextSize(androidTextSizeUnit, androidTextSize)
    }

    fun setFontFamily(fontFamily: Int) {
        this.androidFontFamilyResId = fontFamily
        binding.textView.typeface = ResourcesCompat.getFont(context, fontFamily)
    }

    fun setTextViewAlignment(textAlignment: Int) {
        this.androidTextAlignment = textAlignment
        binding.textView.textAlignment = androidTextAlignment
    }

    fun setTextAllCaps(textAllCaps: Boolean) {
        this.androidTextAllCaps = textAllCaps
        binding.textView.isAllCaps = androidTextAllCaps
    }

    fun setImageSrc(imageSrcId: Int) {
        this.imageSrcId = imageSrcId
        binding.imageView.setImageResource(imageSrcId)
    }

    fun setImageTint(@ColorInt imageTint: Int) {
        this.imageTint = imageTint
        binding.imageView.imageTintList = ColorStateList.valueOf(imageTint)
    }

    fun hide() {
        clearAnimation()
        animate().scaleY(0f).scaleX(0f).setDuration(300).interpolator = AnticipateInterpolator()
    }

    fun show() {
        clearAnimation()
        animate().scaleY(1f).scaleX(1f).setDuration(400).interpolator = OvershootInterpolator()
    }

    private fun pxFromDp(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()

        bundle.putInt("rippleColor", rippleColor)
        bundle.putInt("backgroundColorEnabled", _backgroundColor)
        bundle.putInt("backgroundColorDisabled", backgroundColorDisabled)
        bundle.putInt("strokeColorEnabled", strokeColor)
        bundle.putInt("strokeColorDisabled", strokeColorDisabled)
        bundle.putFloat("strokeWidth", strokeWidth)
        bundle.putFloat("cornerRadius", cornerRadius)
        bundle.putInt("progressBarColor", progressBarColor)

        bundle.putSerializable("loadingButtonState", loadingButtonState)

        bundle.putString("androidText", androidText)
        bundle.putInt("androidTextColor", androidTextColor)
        bundle.putInt("androidTextSizeUnit", androidTextSizeUnit)
        bundle.putFloat("androidTextSize", androidTextSize)
        bundle.putInt("androidTextAlignment", androidTextAlignment)
        bundle.putBoolean("androidTextAllCaps", androidTextAllCaps)

        bundle.putInt("imageScaleType", imageScaleType)
        bundle.putInt("imageSrcId", imageSrcId)
        bundle.putInt("imageTint", imageTint)

        bundle.putParcelable("superState", super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {

        var viewState = state

        if (viewState is Bundle) {

            rippleColor = viewState.getInt("rippleColor")
            _backgroundColor = viewState.getInt("backgroundColorEnabled")
            backgroundColorDisabled = viewState.getInt("backgroundColorDisabled")
            strokeColor = viewState.getInt("strokeColorEnabled")
            strokeColorDisabled = viewState.getInt("strokeColorDisabled")
            strokeWidth = viewState.getFloat("strokeWidth")
            cornerRadius = viewState.getFloat("cornerRadius")
            progressBarColor = viewState.getInt("progressBarColor")

            loadingButtonState = viewState.getSerializable("loadingButtonState") as LoadingButtonState

            androidText = viewState.getString("androidText", "")
            androidTextColor = viewState.getInt("androidTextColor")
            androidTextSizeUnit = viewState.getInt("androidTextSizeUnit")
            androidTextSize = viewState.getFloat("androidTextSize")
            androidTextAlignment = viewState.getInt("androidTextAlignment")
            androidTextAllCaps = viewState.getBoolean("androidTextAllCaps")

            imageScaleType = viewState.getInt("imageScaleType")

            when (imageScaleType) {
                0 -> {
                    imageViewScaleType = ImageView.ScaleType.MATRIX
                }
                1 -> {
                    imageViewScaleType = ImageView.ScaleType.FIT_XY
                }
                2 -> {
                    imageViewScaleType = ImageView.ScaleType.FIT_START
                }
                3 -> {
                    imageViewScaleType = ImageView.ScaleType.FIT_CENTER
                }
                4 -> {
                    imageViewScaleType = ImageView.ScaleType.FIT_END
                }
                5 -> {
                    imageViewScaleType = ImageView.ScaleType.CENTER
                }
                6 -> {
                    imageViewScaleType = ImageView.ScaleType.CENTER_CROP
                }
                7 -> {
                    imageViewScaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            }

            imageSrcId = viewState.getInt("imageSrcId")
            imageTint = viewState.getInt("imageTint")

            viewState = viewState.getParcelable("superState")
        }

        super.onRestoreInstanceState(viewState)

        updateLayout()

        refreshState()
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (gainFocus) {
            if (isEnabled) {
                clearAnimation()
                animate().scaleX(0.95f).scaleY(0.95f).setDuration(150).setInterpolator(FastOutSlowInInterpolator()).start()
            }
        } else {
            if (isEnabled) {
                clearAnimation()
                animate().scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(FastOutSlowInInterpolator()).start()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event != null) {

            if (event.action == MotionEvent.ACTION_UP) {

                if (isEnabled) {
                    clearAnimation()
                    animate().scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(FastOutSlowInInterpolator()).start()
                }
            }

            if (event.action == MotionEvent.ACTION_DOWN) {
                if (isEnabled) {
                    clearAnimation()
                    animate().scaleX(0.95f).scaleY(0.95f).setDuration(150).setInterpolator(FastOutSlowInInterpolator()).start()
                }
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        onTouchEvent(ev)
        return false
    }

    private sealed class LoadingButtonState(val isEnabled: Boolean, val progressBarVisibility: Int, val textViewVisibility: Int, val imageViewVisibility: Int) : Serializable {
        object Normal : LoadingButtonState(true, View.GONE, View.VISIBLE, View.VISIBLE)
        object Loading : LoadingButtonState(false, View.VISIBLE, View.INVISIBLE, View.INVISIBLE)
    }
}