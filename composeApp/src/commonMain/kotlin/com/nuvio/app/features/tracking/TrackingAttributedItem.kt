package com.nuvio.app.features.tracking

interface TrackingAttributedItem {
    val trackingProviderId: String?
    val trackingProviderItemId: String?
    val trackingSourceUrl: String?
    val trackingContentId: String
}
