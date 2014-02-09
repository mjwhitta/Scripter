/**
 * GNU GENERAL PUBLIC LICENSE See the file license.txt for copying conditions.
 * 
 * @author mjwhitta
 * @created Nov 15, 2012
 */
package sites.mjwhitta.scripter;

import sites.mjwhitta.andlib.mjwhitta.dialogs.OutputDialog;
import sites.mjwhitta.andlib.mjwhitta.utils.Shell;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * This class represents a desktop shortcut
 */
public class ShortcutActivity extends SherlockActivity {
   private String m_path = "";

   private Boolean m_runAsRoot = false;

   private Boolean m_hideOutput = false;

   private String m_args = "";

   private String m_name = "";

   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      setTheme(R.style.Theme_Sherlock);
      super.onCreate(savedInstanceState);

      m_name = getIntent().getStringExtra("name");
      m_path = getIntent().getStringExtra("path");
      m_runAsRoot = getIntent().getBooleanExtra("runAsRoot", false);
      m_args = getIntent().getStringExtra("args");
      m_hideOutput = getIntent().getBooleanExtra("hideOutput", false);

      if (m_hideOutput) {
         String command = "sh " + m_path + " " + m_args;
         try {
            if (m_runAsRoot) {
               Shell.executeAsRoot(command);
            } else {
               Shell.execute(command);
            }
            toastMessage("Script " + m_name + " ran successfully");
         } catch (Exception e) {
            toastError("Script " + m_name + " failed!");
         }
         finish();
      } else {
         showDialog(OutputDialog.ID);
      }
   }

   /**
    * @see android.app.Activity#onCreateDialog(int)
    */
   @Override
   protected Dialog onCreateDialog(int id) {
      if (id == OutputDialog.ID) {
         return new OutputDialog(this, "Output:") {
            /**
             * @see sites.mjwhitta.scripter.dialogs.ScriptOutputDialog#getOutput()
             */
            @Override
            public String getOutput() {
               String output = "";
               try {
                  String command = "sh " + m_path + " " + m_args;
                  if (m_runAsRoot) {
                     output = Shell.executeAsRoot(command);
                  } else {
                     output = Shell.execute(command);
                  }
               } catch (Exception e) {
                  output = "Error running script " + m_path + " b/c:\n\n"
                        + e.getMessage();
               }
               return output;
            }

            /**
             * @see sites.mjwhitta.scripter.dialogs.ScriptOutputDialog#onCloseButton()
             */
            @Override
            public void onCloseButton() {
               removeDialog(OutputDialog.ID);
               finish();
            }
         };
      } else {
         return null;
      }
   }

   /**
    * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
    */
   @Override
   protected void onPrepareDialog(int id, Dialog dialog) {
      super.onPrepareDialog(id, dialog);
   }

   /**
    * Display error message in long toast.
    * 
    * @param err
    */
   public void toastError(String err) {
      Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
   }

   /**
    * Display message in short toast.
    * 
    * @param msg
    */
   public void toastMessage(String msg) {
      Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
   }
}
