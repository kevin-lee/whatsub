package whatsub

/** @author Kevin Lee
  * @since 2021-08-15
  */
package object charset {
  
  type Charset = Charset.Charset
  object Charset {
    opaque type Charset = java.nio.charset.Charset
    def apply(charset: java.nio.charset.Charset): Charset = charset
    
    given charsetCanEqual: CanEqual[Charset, Charset] = CanEqual.derived
    
    extension (charset: Charset) {
      def value: java.nio.charset.Charset = charset
    }

    final val Utf8 = Charset(java.nio.charset.StandardCharsets.UTF_8)
  }
  
}
