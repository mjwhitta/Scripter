/**
 * GNU GENERAL PUBLIC LICENSE See the file license.txt for copying conditions.
 * 
 * @author mjwhitta
 * @created Aug 16, 2012
 */
package sites.mjwhitta.scripter;

/**
 * This class just holds a Script object for sharing between activities.
 */
public class ScriptSingleton {
   private static Script INSTANCE;

   /**
    * @return The singleton instance.
    */
   public static Script getInstance() {
      return INSTANCE;
   }

   /**
    * Sets the singleton instance.
    * 
    * @param s
    */
   public static void setInstance(Script s) {
      INSTANCE = s;
   }
}
