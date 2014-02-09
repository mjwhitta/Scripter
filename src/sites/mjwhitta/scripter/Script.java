/**
 * GNU GENERAL PUBLIC LICENSE See the file license.txt for copying conditions.
 * 
 * @author mjwhitta
 * @created Aug 16, 2012
 */
package sites.mjwhitta.scripter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import sites.mjwhitta.andlib.mjwhitta.utils.StringUtils;

/**
 * The interface to be implemented by all scripts.
 */
public class Script {
   private final File m_file;

   private boolean m_isRunAsRoot = false;

   private boolean m_isHideOutput = false;

   private String m_args = "";

   /**
    * Constructor
    * 
    * @param f
    */
   public Script(File f) {
      m_file = f;
   }

   /**
    * Constructor
    * 
    * @param f
    * @param root
    */
   public Script(File f, boolean root) {
      this(f);
      m_isRunAsRoot = root;
   }

   /**
    * @return Script's arguments
    */
   public String getArgs() {
      if (m_args == null) {
         m_args = "";
      }
      return m_args;
   }

   /**
    * @return Name of Script's config file
    */
   public String getConfigName() {
      String parent_path = m_file.getParent();
      String parent = "root";
      if (!StringUtils.isNullorEmpty(parent_path)) {
         String[] temp = parent_path.split("/");
         parent = temp[temp.length - 1];
      }
      return parent + "_" + m_file.getName();
   }

   public String getFullPath() {
      return m_file.getAbsolutePath();
   }

   /**
    * @return The name of the Script.
    */
   public String getName() {
      return m_file.getName();
   }

   /**
    * @return The parent directory
    */
   public String getParentDir() {
      return m_file.getParent();
   }

   /**
    * @return Should the output be hidden
    */
   public boolean hideOutput() {
      return m_isHideOutput;
   }

   /**
    * Set whether the output should be hidden or not
    * 
    * @param hide
    * @return This Script
    */
   public Script hideOutput(boolean hide) {
      m_isHideOutput = hide;
      return this;
   }

   /**
    * Restore Script attributes from private config file
    * 
    * @param fis
    * @throws IOException
    */
   public void restoreAttributes(FileInputStream fis) throws IOException {
      // Restore Script's attributes from config file
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      m_isRunAsRoot = br.readLine().trim().equals("1");
      String args = "";
      if ((args = br.readLine()) != null) {
         m_args = args.trim();
      }
      String hide = "";
      if ((hide = br.readLine()) != null) {
         m_isHideOutput = hide.trim().equals("1");
      } else {
         m_isHideOutput = false;
      }
      br.close();
   }

   /**
    * @return Is the Script run as root
    */
   public boolean runAsRoot() {
      return m_isRunAsRoot;
   }

   /**
    * Set whether or not Script is run as root
    * 
    * @param root
    * @return This Script
    */
   public Script runAsRoot(boolean root) {
      m_isRunAsRoot = root;
      return this;
   }

   /**
    * Save Script attributes to private config file
    * 
    * @param fos
    * @throws IOException
    */
   public void saveAttributes(FileOutputStream fos) throws IOException {
      // Save Script's attributes to config file
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
      bw.write(m_isRunAsRoot ? "1" : "0");
      bw.write("\n");
      bw.write(m_args);
      bw.write("\n");
      bw.write(m_isHideOutput ? "1" : "0");
      bw.close();
   }

   /**
    * Sets the Script arguments to args
    * 
    * @param args
    */
   public Script setArgs(String args) {
      m_args = args;
      return this;
   }

   /**
    * @return The Script's name
    */
   @Override
   public String toString() {
      return m_file.getName();
   }
}
