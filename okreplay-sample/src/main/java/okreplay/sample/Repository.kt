package okreplay.sample

import com.google.auto.value.AutoValue
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okreplay.sample.AutoValue_Repository.MoshiJsonAdapter

@AutoValue
internal abstract class Repository {
    abstract fun name(): String?
    abstract fun description(): String?

    companion object {
        @JvmStatic
        fun jsonAdapter(moshi: Moshi?): JsonAdapter<Repository> = MoshiJsonAdapter(moshi)
    }
}
