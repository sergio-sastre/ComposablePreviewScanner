package sergio.sastre.composable.preview.scanner.android.device.domain

data class Identifier(val id: String? = null, val name: String? = null) {
    companion object {
        // Phones
        val GALAXY_NEXUS = Identifier(id = "Galaxy Nexus", name = "Galaxy Nexus")
        val NEXUS_S = Identifier(id = "Nexus S", name = "Nexus S")
        val NEXUS_ONE = Identifier(id = "Nexus One", name = "Nexus One")
        val NEXUS_4 = Identifier(id = "Nexus 4", name = "Nexus 4")
        val NEXUS_5 = Identifier(id = "Nexus 5", name = "Nexus 5")
        val NEXUS_5X = Identifier(id = "Nexus 5X", name = "Nexus 5X")
        val NEXUS_6 = Identifier(id = "Nexus 6", name = "Nexus 6")
        val NEXUS_6P = Identifier(id = "Nexus 6P", name = "Nexus 6P")

        // @Preview parameter device "id:Nexus 7" and "name:Nexus 7" point indeed to different devices
        // This matching problem is likely an error in Android Studio, but we want to keep it consistent with it
        val NEXUS_7_2012 = Identifier(id = "Nexus 7", name = "Nexus 7 (2012)")
        val NEXUS_7_2013 = Identifier(id = "Nexus 7 2013", name = "Nexus 7")

        val NEXUS_9 = Identifier(id = "Nexus 9", name = "Nexus 9")
        val NEXUS_10 = Identifier(id = "Nexus 10", name = "Nexus 10")
        val PIXEL = Identifier(id = "pixel", name = "Pixel")
        val PIXEL_XL = Identifier(id = "pixel_xl", name = "Pixel XL")
        val PIXEL_2 = Identifier(id = "pixel_2", name = "Pixel 2")
        val PIXEL_2_XL = Identifier(id = "pixel_2_xl", name = "Pixel 2 XL")
        val PIXEL_3 = Identifier(id = "pixel_3", name = "Pixel 3")
        val PIXEL_3A = Identifier(id = "pixel_3a", name = "Pixel 3a")
        val PIXEL_3_XL = Identifier(id = "pixel_3_xl", name = "Pixel 3 XL")
        val PIXEL_3A_XL = Identifier(id = "pixel_3a_xl", name = "Pixel 3a XL")
        val PIXEL_4 = Identifier(id = "pixel_4", name = "Pixel 4")
        val PIXEL_4A = Identifier(id = "pixel_4a", name = "Pixel 4a")
        val PIXEL_4_XL = Identifier(id = "pixel_4_xl", name = "Pixel 4 XL")
        val PIXEL_5 = Identifier(id = "pixel_5", name = "Pixel 5")
        val PIXEL_6 = Identifier(id = "pixel_6", name = "Pixel 6")
        val PIXEL_6A = Identifier(id = "pixel_6a", name = "Pixel 6a")
        val PIXEL_6_PRO = Identifier(id = "pixel_6_pro", name = "Pixel 6 Pro")
        val PIXEL_7 = Identifier(id = "pixel_7", name = "Pixel 7")
        val PIXEL_7A = Identifier(id = "pixel_7a", name = "Pixel 7a")
        val PIXEL_7_PRO = Identifier(id = "pixel_7_pro", name = "Pixel 7 Pro")
        val PIXEL_8 = Identifier(id = "pixel_8", name = "Pixel 8")
        val PIXEL_8A = Identifier(id = "pixel_8a", name = "Pixel 8a")
        val PIXEL_8_PRO = Identifier(id = "pixel_8_pro", name = "Pixel 8 Pro")
        val PIXEL_9 = Identifier(id = "pixel_9", name = "Pixel 9")
        val PIXEL_9A = Identifier(id = "pixel_9a", name = "Pixel 9a")
        val PIXEL_9_PRO = Identifier(id = "pixel_9_pro", name = "Pixel 9 Pro")
        val PIXEL_9_PRO_XL = Identifier(id = "pixel_9_pro_xl", name = "Pixel 9 Pro XL")
        val PIXEL_9_PRO_FOLD = Identifier(id = "pixel_9_pro_fold", name = "Pixel 9 Pro Fold")
        val PIXEL_TABLET = Identifier(id = "pixel_tablet", name = "Pixel Tablet")
        val PIXEL_C = Identifier(id = "pixel_c", name = "Pixel C")
        val PIXEL_FOLD = Identifier(id = "pixel_fold", name = "Pixel Fold")

        // Wear
        val WEAR_OS_SQUARE = Identifier(id = "wearos_square", name = "Wear OS Square")
        val WEAR_OS_RECT = // This is the updated Identifier of WEAR_OS_RECTANGULAR
            Identifier(id = "wearos_rect", name = "Wear OS Rectangular")
        @Deprecated(
            message = "Replaced by WEAR_OS_RECT in newer Android Studio versions",
            replaceWith = ReplaceWith("Identifier.WEAR_OS_RECT")
        )
        val WEAR_OS_RECTANGULAR =
            Identifier(id = "wearos_rectangular", name = "Wear OS Rectangular")
        val WEAR_OS_SMALL_ROUND =
            Identifier(id = "wearos_small_round", name = "Wear OS Small Round")
        val WEAR_OS_LARGE_ROUND =
            Identifier(id = "wearos_large_round", name = "Wear OS Large Round")

        // Desktop
        val SMALL_DESKTOP = Identifier(id = "desktop_small", name = "Small Desktop")
        val MEDIUM_DESKTOP = Identifier(id = "desktop_medium", name = "Medium Desktop")
        val LARGE_DESKTOP = Identifier(id = "desktop_large", name = "Large Desktop")

        // Automotive
        val AUTOMOTIVE_ULTRAWIDE =
            Identifier(id = "automotive_ultrawide", name = "Automotive Ultrawide")
        val AUTOMOTIVE_PORTRAIT =
            Identifier(id = "automotive_portrait", name = "Automotive Portrait")
        val AUTOMOTIVE_LARGE_PORTRAIT =
            Identifier(id = "automotive_large_portrait", name = "Automotive Large Portrait")
        val AUTOMOTIVE_DISTANT_DISPLAY =
            Identifier(id = "automotive_distant_display", name = "Automotive Distant Display")
        val AUTOMOTIVE_DISTANT_DISPLAY_WITH_GOOGLE_PLAY = Identifier(
            id = "automotive_distant_display_with_play",
            name = "Automotive Distant Display with Google Play"
        )
        val AUTOMOTIVE_1024DP_LANDSCAPE =
            Identifier(id = "automotive_1024p_landscape", name = "Automotive (1024p landscape)")
        val AUTOMOTIVE_1080DP_LANDSCAPE =
            Identifier(id = "automotive_1080p_landscape", name = "Automotive (1080p landscape)")
        val AUTOMOTIVE_1408DP_LANDSCAPE = Identifier(
            id = "automotive_1408p_landscape_with_google_apis",
            name = "Automotive (1408p landscape)"
        )
        val AUTOMOTIVE_1408DP_LANDSCAPE_WITH_GOOGLE_PLAY = Identifier(
            id = "automotive_1408p_landscape_with_play",
            name = "Automotive (1408p landscape) with Google Play"
        )

        // Television
        val TV_720p = Identifier(id = "tv_720p", name = "Television (720p)")
        val TV_4K = Identifier(id = "tv_4k", name = "Television (4K)")
        val TV_1080p = Identifier(id = "tv_1080p", name = "Television (1080p)")

        // XR Devices
        @Deprecated(
            message = "Replaced by XR_HEADSET in newer Android Studio versions",
            replaceWith = ReplaceWith("Identifier.XR_HEADSET")
        )
        val XR_DEVICE = Identifier(id = "xr_device", name = "XR Device")
        val XR_HEADSET = // This is the updated Identifier of XR_DEVICE
            Identifier(id = "xr_headset_device", name = "XR Headset")

        // Generic Devices
        val MEDIUM_TABLET = Identifier(id = "medium_tablet", name = "Medium Tablet")
        val SMALL_PHONE = Identifier(id = "small_phone", name = "Small Phone")
        val MEDIUM_PHONE = Identifier(id = "medium_phone", name = "Medium Phone")
        val RESIZABLE_EXPERIMENTAL = Identifier(id = "resizable", name = "Resizable (Experimental)")
        val FWVGA_3_7IN_SLIDER = Identifier(id = "3.7 FWVGA slider", name = null)
        val FWVGA_5_4IN = Identifier(id = "5.4in FWVGA", name = null)
        val HVGA_3_2IN_ADP1 = Identifier(id = "3.2in HVGA slider (ADP1)", name = null)
        val QVGA_2_7IN = Identifier(id = "2.7in QVGA", name = null)
        val QVGA_2_7IN_SLIDER = Identifier(id = "2.7in QVGA slider", name = null)
        val QVGA_3_2IN_ADP2 = Identifier(id = "3.2in QVGA (ADP2)", name = null)
        val WQVGA_3_3IN = Identifier(id = "3.3in WQVGA", name = null)
        val WQVGA_3_4IN = Identifier(id = "3.4in WQVGA", name = null)
        val WSVGA_TABLET_7IN = Identifier(id = "7in WSVGA (Tablet)", name = null)
        val NEXUS_ONE_WVGA_3_7IN = Identifier(id = "3.7in WVGA (Nexus One)", name = null)
        val WVGA_5_1IN = Identifier(id = "5.1in WVGA", name = null)
        val WXGA_4_7IN = Identifier(id = "4.7in WXGA", name = null)
        val NEXUS_S_WVGA_4IN = Identifier(id = "4in WVGA (Nexus S)", name = null)
        val GALAXY_NEXUS_4_65IN_720P = Identifier(id = "4.65in 720p (Galaxy Nexus)", name = null)
        val FREEFORM_13_5IN = Identifier(id = "13.5in Freeform", name = null)
        val FOLD_OUT_8IN = Identifier(id = "8in Foldable", name = null)
        val FOLD_IN_WITH_OUTER_DISPLAY_7_6IN = Identifier(id = "7.6in Foldable", name = null)
        val HORIZONTAL_FOLD_IN_6_7IN = Identifier(id = "6.7in Foldable", name = null)
        val ROLLABLE_7_4IN = Identifier(id = "7.4in Rollable", name = null)
        val WXGA_TABLET_10_1IN = Identifier(id = "10.1in WXGA (Tablet)", name = null)
    }
}