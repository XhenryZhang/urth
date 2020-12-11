package edu.ucsb.cs.cs184.urth

data class UserPreferences(
    var defaultSort: DefaultSort,
    var recencyFilter: RecencyFilter,
    var maxArticles: MaxArticles,
    var searchRadius: SearchRadius,
    var expandSearch: Boolean,
    var searchArticleBody: Boolean
) {
    constructor() : this(
        DefaultSort.BY_POPULARITY,
        RecencyFilter.PAST_DAY,
        MaxArticles.TEN,
        SearchRadius.TWENTY,
        false,
        false
    )
}

enum class DefaultSort {
    BY_DATE, BY_RELEVANCY, BY_POPULARITY
}

enum class RecencyFilter {
    PAST_DAY, PAST_WEEK, PAST_MONTH
}

enum class MaxArticles {
    FIVE, TEN, TWENTY
}

enum class SearchRadius {
    TWENTY, FORTY, SIXTY
}
