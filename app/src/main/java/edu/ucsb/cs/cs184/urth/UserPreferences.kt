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

enum class DefaultSort(val sortMethod: String) {
    BY_DATE("publishedAt"),
    BY_RELEVANCY("relevance"),
    BY_POPULARITY("popularity")
}

enum class RecencyFilter(val duration: Int) {
    PAST_DAY(1),
    PAST_WEEK(7),
    PAST_MONTH(1)
}

enum class MaxArticles {
    FIVE, TEN, TWENTY
}

enum class SearchRadius(val km: Int) {
    TWENTY(20), FORTY(40), SIXTY(60)
}
