package okreplay

import android.content.Context
import java.io.Reader

open class AssetManager(internal val context: Context) {
  fun open(file: String): Reader =
      context.assets.open(file).bufferedReader()

  fun exists(path: String, fileName: String) =
      context.assets.list(path)?.contains(fileName)
}