package it.emanuelebriano.owl;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import android.util.Log;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat  {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey) {

        Log.i(Constants.AppTAG, "SettingsFragment.onCreatePreferences()");

        setPreferencesFromResource(R.xml.preferences, rootKey);

    }

}
