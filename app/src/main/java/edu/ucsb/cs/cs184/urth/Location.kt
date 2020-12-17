package edu.ucsb.cs.cs184.urth

data class Location(
    val city: String?,
    val state: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double
) {
    constructor() : this("", "", "", 0.0, 0.0)

    override fun equals(other: Any?): Boolean {
        return if (other is Location) {
            city.equals(other.city) && state.equals(other.state) && country.equals(other.country)
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun toString(): String {
        return "$city, $state, $country"
    }
}