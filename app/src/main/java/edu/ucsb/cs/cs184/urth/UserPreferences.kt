package edu.ucsb.cs.cs184.urth

data class UserPreferences(
    var defaultSort: DefaultSort,
    var recencyFilter: RecencyFilter,
    var maxArticles: MaxArticles,
    var searchRadius: SearchRadius,
    var searchArticleBody: Boolean
) {
    constructor() : this(
        DefaultSort.BY_POPULARITY,
        RecencyFilter.PAST_DAY,
        MaxArticles.TWENTY,
        SearchRadius.TWENTY,
        false
    )

    // For debugging
    override fun toString(): String {
        return "{ " +
                "defaultSort: $defaultSort, " +
                "recencyFilter: $recencyFilter, " +
                "maxArticles: $maxArticles, " +
                "searchRadius: $searchRadius, " +
                "searchArticleBody: $searchArticleBody" +
                " }"
    }
}

enum class Preference(val key: String) {
    DefaultSort("defaultSort"),
    RecencyFilter("recencyFilter"),
    MaxArticles("maxArticles"),
    SearchRadius("searchRadius"),
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

enum class MaxArticles(val pageSize: Int) {
    TEN(10),
    TWENTY(20),
    FIFTY(50)
}

enum class SearchRadius(val km: Int) {
    TWENTY(20),
    FORTY(40),
    SIXTY(60)
}
