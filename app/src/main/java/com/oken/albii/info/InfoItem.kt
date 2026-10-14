package com.oken.albii.info

import androidx.annotation.StringRes
import com.oken.albii.R

/** 写真情報のカテゴリ。設定画面で一括 ON/OFF の単位になる。 */
enum class InfoCategory(@StringRes val labelRes: Int) {
    EXPOSURE(R.string.info_cat_exposure),
    LENS(R.string.info_cat_lens),
    DEVICE(R.string.info_cat_device),
    DATETIME(R.string.info_cat_datetime),
    IMAGE(R.string.info_cat_image),
}

/** 詳細シートに出せる項目。[defaultOn] は初回起動時の既定値（シリアル・GPS は OPT-IN）。 */
enum class InfoItem(
    val category: InfoCategory,
    @StringRes val labelRes: Int,
    val defaultOn: Boolean,
) {
    ISO(InfoCategory.EXPOSURE, R.string.info_iso, true),
    SHUTTER(InfoCategory.EXPOSURE, R.string.info_shutter, true),
    APERTURE(InfoCategory.EXPOSURE, R.string.info_aperture, true),
    EXPOSURE_BIAS(InfoCategory.EXPOSURE, R.string.info_exposure_bias, true),
    EXPOSURE_PROGRAM(InfoCategory.EXPOSURE, R.string.info_exposure_program, true),
    METERING(InfoCategory.EXPOSURE, R.string.info_metering, true),
    WHITE_BALANCE(InfoCategory.EXPOSURE, R.string.info_white_balance, true),
    FLASH(InfoCategory.EXPOSURE, R.string.info_flash, true),

    FOCAL_LENGTH(InfoCategory.LENS, R.string.info_focal_length, true),
    FOCAL_LENGTH_35(InfoCategory.LENS, R.string.info_focal_length_35, true),
    MAX_APERTURE(InfoCategory.LENS, R.string.info_max_aperture, true),
    LENS_MODEL(InfoCategory.LENS, R.string.info_lens_model, true),

    MAKE(InfoCategory.DEVICE, R.string.info_make, false),
    MODEL(InfoCategory.DEVICE, R.string.info_model, false),
    SOFTWARE(InfoCategory.DEVICE, R.string.info_software, false),

    CAPTURED_AT(InfoCategory.DATETIME, R.string.info_captured_at, true),
    GPS(InfoCategory.DATETIME, R.string.info_gps, false),

    DIMENSIONS(InfoCategory.IMAGE, R.string.info_dimensions, true),
    FILE_SIZE(InfoCategory.IMAGE, R.string.info_file_size, true),
    FORMAT(InfoCategory.IMAGE, R.string.info_format, true),
    ORIENTATION(InfoCategory.IMAGE, R.string.info_orientation, false),
    COLOR_SPACE(InfoCategory.IMAGE, R.string.info_color_space, false),
    ;

    companion object {
        val defaults: Set<InfoItem> = entries.filter { it.defaultOn }.toSet()
    }
}
