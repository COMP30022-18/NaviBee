package au.edu.unimelb.eng.navibee.utils

class Resource<T>(val status: Status,
                  val data: T?,
                  val throwable: Throwable?) {

    companion object {
        fun <T> success(data: T) =
                Resource(Status.SUCCESS, data, null)

        fun <T> error(throwable: Throwable, data: T? = null) =
                Resource(Status.ERROR, data, throwable)

        fun <T> loading(data: T?) =
                Resource(Status.LOADING, data, null)
    }

    enum class Status {
        SUCCESS, ERROR, LOADING
    }
}