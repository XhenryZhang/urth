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

    // For debugging
    override fun toString(): String {
        return "{ " +
                "defaultSort: $defaultSort, " +
                "recencyFilter: $recencyFilter, " +
                "maxArticles: $maxArticles, " +
                "searchRadius: $searchRadius, " +
                "expandSearch: $expandSearch, " +
                "searchArticleBody: $searchArticleBody" +
                " }"
    }
}

enum class Preference(val key: String) {
    DefaultSort("defaultSort"),
    RecencyFilter("recencyFilter"),
    MaxArticles("maxArticles"),
    SearchRadius("searchRadius"),
    ExpandSearch("expandSearch"),
    SearchArticleBody("searchArticleBody")
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
