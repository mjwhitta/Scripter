/**
 * GNU GENERAL PUBLIC LICENSE See the file license.txt for copying conditions.
 * 
 * @author mjwhitta
 * @created Aug 16, 2012
 */
package sites.mjwhitta.scripter.dialogs;

import sites.mjwhitta.scripter.R;
import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * The dialog created when the user selects "Add Script" from the menu.
 */
public abstract class NewScriptDialog extends Dialog {
   private final Activity m_parentActivity;

   /**
    * Constructor
    * 
    * @param activity
    */
   public NewScriptDialog(final Activity activity,
         final ArrayAdapter<String> parentDirs) {
      super(activity);

      m_parentActivity = activity;

      setContentView(R.layout.new_script_dialog);
      setTitle("Create New Script");

      final Spinner parentDir = (Spinner) findViewById(R.id.parent_dir_spinner);
      parentDir.setAdapter(parentDirs);

      final EditText scriptNameTextField = (EditText) findViewById(R.id.script_name);

      final Button okButton = (Button) findViewById(R.id.script_ok_button);
      okButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String name = scriptNameTextField.getText().toString().trim();
            if (!name.equals("") && !name.contains(" ") && !name.contains("/")) {
               onOkButton(parentDir.getSelectedItemPosition(), name);
            } else {
               toastError("Script names can't have spaces or file separators in them!");
            }
         }
      });
   }

   /**
    * Actions to take when the OK button is clicked.
    * 
    * @param newScriptName
    */
   public abstract void onOkButton(int parentDir, String newScriptName);

   private void toastError(String err) {
      Toast.makeText(m_parentActivity.getApplicationContext(), err,
            Toast.LENGTH_LONG).show();
   }
}
