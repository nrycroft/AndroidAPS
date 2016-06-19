package info.nightscout.androidaps.plugins.ConfigBuilder;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import info.nightscout.androidaps.Config;
import info.nightscout.androidaps.MainActivity;
import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.R;
import info.nightscout.androidaps.data.Result;
import info.nightscout.androidaps.db.TempBasal;
import info.nightscout.androidaps.events.EventRefreshGui;
import info.nightscout.androidaps.interfaces.APSInterface;
import info.nightscout.androidaps.interfaces.ConstrainsInterface;
import info.nightscout.androidaps.interfaces.PluginBase;
import info.nightscout.androidaps.interfaces.ProfileInterface;
import info.nightscout.androidaps.interfaces.PumpInterface;
import info.nightscout.androidaps.interfaces.TempBasalsInterface;
import info.nightscout.androidaps.interfaces.TreatmentsInterface;
import info.nightscout.androidaps.plugins.APSResult;
import info.nightscout.androidaps.plugins.TempBasals.TempBasalsFragment;
import info.nightscout.androidaps.plugins.Treatments.TreatmentsFragment;
import info.nightscout.client.data.NSProfile;

public class ConfigBuilderFragment extends Fragment implements PluginBase, PumpInterface, ConstrainsInterface {
    private static Logger log = LoggerFactory.getLogger(ConfigBuilderFragment.class);

    private static final String PREFS_NAME = "Settings";

    ListView pumpListView;
    ListView loopListView;
    ListView treatmentsListView;
    ListView tempsListView;
    ListView profileListView;
    ListView apsListView;
    ListView constrainsListView;
    ListView generalListView;

    PluginCustomAdapter pumpDataAdapter = null;
    PluginCustomAdapter loopDataAdapter = null;
    PluginCustomAdapter treatmentsDataAdapter = null;
    PluginCustomAdapter tempsDataAdapter = null;
    PluginCustomAdapter profileDataAdapter = null;
    PluginCustomAdapter apsDataAdapter = null;
    PluginCustomAdapter constrainsDataAdapter = null;
    PluginCustomAdapter generalDataAdapter = null;


    PumpInterface activePump;
    ProfileInterface activeProfile;
    TreatmentsInterface activeTreatments;
    TempBasalsInterface activeTempBasals;

    ArrayList<PluginBase> pluginList;

    public ConfigBuilderFragment() {
        super();
        registerBus();
    }

    public void initialize() {
        pluginList = MainActivity.getPluginsList();
        loadSettings();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void registerBus() {
        try {
            MainApp.bus().unregister(this);
        } catch (RuntimeException x) {
            // Ignore
        }
        MainApp.bus().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.configbuilder_fragment, container, false);
        pumpListView = (ListView) view.findViewById(R.id.configbuilder_pumplistview);
        loopListView = (ListView) view.findViewById(R.id.configbuilder_looplistview);
        treatmentsListView = (ListView) view.findViewById(R.id.configbuilder_treatmentslistview);
        tempsListView = (ListView) view.findViewById(R.id.configbuilder_tempslistview);
        profileListView = (ListView) view.findViewById(R.id.configbuilder_profilelistview);
        apsListView = (ListView) view.findViewById(R.id.configbuilder_apslistview);
        constrainsListView = (ListView) view.findViewById(R.id.configbuilder_constrainslistview);
        generalListView = (ListView) view.findViewById(R.id.configbuilder_generallistview);

        setViews();
        return view;
    }

    void setViews() {
        pumpDataAdapter = new PluginCustomAdapter(getContext(), R.layout.configbuilder_simpleitem, MainActivity.getSpecificPluginsList(PluginBase.PUMP));
        pumpListView.setAdapter(pumpDataAdapter);
        setListViewHeightBasedOnChildren(pumpListView);
        loopDataAdapter = new PluginCustomAdapter(getContext(), R.layout.configbuilder_simpleitem, MainActivity.getSpecificPluginsList(PluginBase.LOOP));
        loopListView.setAdapter(loopDataAdapter);
        setListViewHeightBasedOnChildren(loopListView);
        treatmentsDataAdapter = new PluginCustomAdapter(getContext(), R.layout.configbuilder_simpleitem, MainActivity.getSpecificPluginsList(PluginBase.TREATMENT));
        treatmentsListView.setAdapter(treatmentsDataAdapter);
        setListViewHeightBasedOnChildren(treatmentsListView);
        tempsDataAdapter = new PluginCustomAdapter(getContext(), R.layout.configbuilder_simpleitem, MainActivity.getSpecificPluginsList(PluginBase.TEMPBASAL));
        tempsListView.setAdapter(tempsDataAdapter);
        setListViewHeightBasedOnChildren(tempsListView);
        profileDataAdapter = new PluginCustomAdapter(getContext(), R.layout.configbuilder_simpleitem, MainActivity.getSpecificPluginsList(PluginBase.PROFILE));
        profileListView.setAdapter(profileDataAdapter);
        setListViewHeightBasedOnChildren(profileListView);
        apsDataAdapter = new PluginCustomAdapter(getContext(), R.layout.configbuilder_simpleitem, MainActivity.getSpecificPluginsList(PluginBase.APS));
        apsListView.setAdapter(apsDataAdapter);
        setListViewHeightBasedOnChildren(apsListView);
        constrainsDataAdapter = new PluginCustomAdapter(getContext(), R.layout.configbuilder_simpleitem, MainActivity.getSpecificPluginsList(PluginBase.CONSTRAINS));
        constrainsListView.setAdapter(constrainsDataAdapter);
        setListViewHeightBasedOnChildren(constrainsListView);
        generalDataAdapter = new PluginCustomAdapter(getContext(), R.layout.configbuilder_simpleitem, MainActivity.getSpecificPluginsList(PluginBase.GENERAL));
        generalListView.setAdapter(generalDataAdapter);
        setListViewHeightBasedOnChildren(generalListView);

    }

    /*
     * PluginBase interface
     */
    @Override
    public int getType() {
        return PluginBase.GENERAL;
    }

    @Override
    public String getName() {
        return MainApp.instance().getString(R.string.configbuilder);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isVisibleInTabs() {
        return true;
    }

    @Override
    public boolean canBeHidden() {
        return false;
    }

    @Override
    public void setFragmentEnabled(boolean fragmentEnabled) {
        // Always enabled
    }

    @Override
    public void setFragmentVisible(boolean fragmentVisible) {
        // Always visible
    }

    public static ConfigBuilderFragment newInstance() {
        ConfigBuilderFragment fragment = new ConfigBuilderFragment();
        return fragment;
    }


    /*
     * Pump interface
     *
     * Config builder return itself as a pump and check constrains before it passes command to pump driver
     */
    @Nullable
    public PumpInterface getActivePump() {
        return this;
    }

    @Override
    public boolean isTempBasalInProgress() {
        return activePump.isTempBasalInProgress();
    }

    @Override
    public boolean isExtendedBoluslInProgress() {
        return activePump.isExtendedBoluslInProgress();
    }

    @Override
    public Integer getBatteryPercent() {
        return activePump.getBatteryPercent();
    }

    @Override
    public Integer getReservoirValue() {
        return activePump.getReservoirValue();
    }

    @Override
    public void setNewBasalProfile(NSProfile profile) {
        activePump.setNewBasalProfile(profile);
    }

    @Override
    public double getBaseBasalRate() {
        return activePump.getBaseBasalRate();
    }

    @Override
    public double getTempBasalAbsoluteRate() {
        return activePump.getTempBasalAbsoluteRate();
    }

    @Override
    public double getTempBasalRemainingMinutes() {
        return activePump.getTempBasalRemainingMinutes();
    }

    @Override
    public Result deliverTreatment(Double insulin, Double carbs) {
        // TODO: constrains here
        return activePump.deliverTreatment(insulin, carbs);
    }

    @Override
    public Result setTempBasalAbsolute(Double absoluteRate, Integer durationInMinutes) {
        // TODO: constrains here
        return activePump.setTempBasalAbsolute(absoluteRate, durationInMinutes);
    }

    @Override
    public Result setTempBasalPercent(Integer percent, Integer durationInMinutes) {
        // TODO: constrains here
        return activePump.setTempBasalPercent(percent, durationInMinutes);
    }

    @Override
    public Result setExtendedBolus(Double insulin, Integer durationInMinutes) {
        // TODO: constrains here
        return activePump.setExtendedBolus(insulin, durationInMinutes);
    }

    @Override
    public Result cancelTempBasal() {
        return activePump.cancelTempBasal();
    }

    @Override
    public Result cancelExtendedBolus() {
        return activePump.cancelExtendedBolus();
    }

    @Override
    public Result applyAPSRequest(APSResult request) {
        return activePump.applyAPSRequest(request);
    }

    @Override
    public JSONObject getJSONStatus() {
        return activePump.getJSONStatus();
    }

    /*
     * ConfigBuilderFragment code
     */

    private class PluginCustomAdapter extends ArrayAdapter<PluginBase> {

        private ArrayList<PluginBase> pluginList;

        public PluginCustomAdapter(Context context, int textViewResourceId,
                                   ArrayList<PluginBase> pluginList) {
            super(context, textViewResourceId, pluginList);
            this.pluginList = new ArrayList<PluginBase>();
            this.pluginList.addAll(pluginList);
        }

        private class PluginViewHolder {
            TextView name;
            CheckBox checkboxEnabled;
            CheckBox checkboxVisible;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            PluginViewHolder holder = null;

            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.configbuilder_simpleitem, null);

                holder = new PluginViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.configbuilder_simpleitem_name);
                holder.checkboxEnabled = (CheckBox) convertView.findViewById(R.id.configbuilder_simpleitem_checkboxenabled);
                holder.checkboxVisible = (CheckBox) convertView.findViewById(R.id.configbuilder_simpleitem_checkboxvisible);
                convertView.setTag(holder);

                holder.checkboxEnabled.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        PluginBase plugin = (PluginBase) cb.getTag();
                        plugin.setFragmentEnabled(cb.isChecked());
                        if (cb.isChecked()) plugin.setFragmentVisible(true);
                        onEnabledCategoryChanged(plugin);
                        storeSettings();
                    }
                });

                holder.checkboxVisible.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        PluginBase plugin = (PluginBase) cb.getTag();
                        plugin.setFragmentVisible(cb.isChecked());
                        storeSettings();
                        MainApp.bus().post(new EventRefreshGui());
                    }
                });
            } else {
                holder = (PluginViewHolder) convertView.getTag();
            }

            PluginBase plugin = pluginList.get(position);
            holder.name.setText(plugin.getName());
            holder.checkboxEnabled.setChecked(plugin.isEnabled());
            holder.checkboxVisible.setChecked(plugin.isVisibleInTabs());
            holder.name.setTag(plugin);
            holder.checkboxEnabled.setTag(plugin);
            holder.checkboxVisible.setTag(plugin);

            if (!plugin.canBeHidden()) {
                holder.checkboxEnabled.setEnabled(false);
                holder.checkboxVisible.setEnabled(false);
            }

            int type = plugin.getType();
            // Force enabled if there is only one plugin
            if (type == PluginBase.PUMP || type == PluginBase.TREATMENT || type == PluginBase.TEMPBASAL || type == PluginBase.PROFILE)
                if (pluginList.size() < 2)
                    holder.checkboxEnabled.setEnabled(false);

            // Constrains cannot be disabled
            if (type == PluginBase.CONSTRAINS)
                holder.checkboxEnabled.setEnabled(false);

            // Hide disabled profiles by default
            if (type == PluginBase.PROFILE) {
                if (!plugin.isEnabled()) {
                    holder.checkboxVisible.setEnabled(false);
                    holder.checkboxVisible.setChecked(false);
                } else {
                    holder.checkboxVisible.setEnabled(true);
                }
            }

            return convertView;

        }
    }

    @Nullable
    public ProfileInterface getActiveProfile() {
        return activeProfile;
    }

    @Nullable
    public TreatmentsInterface getActiveTreatments() {
        return activeTreatments;
    }

    @Nullable
    public TempBasalsInterface getActiveTempBasals() {
        return activeTempBasals;
    }

    void onEnabledCategoryChanged(PluginBase changedPlugin) {
        int category = changedPlugin.getType();
        ArrayList<PluginBase> pluginsInCategory = MainActivity.getSpecificPluginsList(category);
        switch (category) {
            // Multiple selection allowed
            case PluginBase.APS:
            case PluginBase.GENERAL:
            case PluginBase.CONSTRAINS:
                break;
            // Single selection allowed
            case PluginBase.PROFILE:
            case PluginBase.PUMP:
            case PluginBase.LOOP:
            case PluginBase.TEMPBASAL:
            case PluginBase.TREATMENT:
                boolean newSelection = changedPlugin.isEnabled();
                if (newSelection) { // new plugin selected -> disable others
                    for (PluginBase p : pluginsInCategory) {
                        if (p.getName().equals(changedPlugin.getName())) {
                            // this is new selected
                        } else {
                            p.setFragmentEnabled(false);
                            setViews();
                        }
                    }
                } else { // enable first plugin in list
                    pluginsInCategory.get(0).setFragmentEnabled(true);
                }
                break;
        }
    }

    private void verifySelectionInCategories() {
        for (int category : new int[]{PluginBase.GENERAL, PluginBase.APS, PluginBase.PROFILE, PluginBase.PUMP, PluginBase.LOOP, PluginBase.TEMPBASAL, PluginBase.TREATMENT}) {
            ArrayList<PluginBase> pluginsInCategory = MainActivity.getSpecificPluginsList(category);
            switch (category) {
                // Multiple selection allowed
                case PluginBase.APS:
                case PluginBase.GENERAL:
                case PluginBase.CONSTRAINS:
                    break;
                // Single selection allowed
                case PluginBase.PROFILE:
                    activeProfile = (ProfileInterface) getTheOneEnabledInArray(pluginsInCategory);
                    if (Config.logConfigBuilder)
                        log.debug("Selected profile interface: " + ((PluginBase) activeProfile).getName());
                    for (PluginBase p : pluginsInCategory) {
                        if (!p.getName().equals(((PluginBase) activeProfile).getName())) {
                            p.setFragmentVisible(false);
                        }
                    }
                    break;
                case PluginBase.PUMP:
                    activePump = (PumpInterface) getTheOneEnabledInArray(pluginsInCategory);
                    if (Config.logConfigBuilder)
                        log.debug("Selected pump interface: " + ((PluginBase) activePump).getName());
                    for (PluginBase p : pluginsInCategory) {
                        if (!p.getName().equals(((PluginBase) activePump).getName())) {
                            p.setFragmentVisible(false);
                        }
                    }
                    break;
                case PluginBase.LOOP:
                    PluginBase loop = getTheOneEnabledInArray(pluginsInCategory);
                    if (Config.logConfigBuilder)
                        log.debug("Selected loop interface: " + loop.getName());
                    for (PluginBase p : pluginsInCategory) {
                        if (!p.getName().equals(loop.getName())) {
                            p.setFragmentVisible(false);
                        }
                    }
                    break;
                case PluginBase.TEMPBASAL:
                    activeTempBasals = (TempBasalsInterface) getTheOneEnabledInArray(pluginsInCategory);
                    if (Config.logConfigBuilder)
                        log.debug("Selected tempbasal interface: " + ((PluginBase) activeTempBasals).getName());
                    for (PluginBase p : pluginsInCategory) {
                        if (!p.getName().equals(((PluginBase) activeTempBasals).getName())) {
                            p.setFragmentVisible(false);
                        }
                    }
                    break;
                case PluginBase.TREATMENT:
                    activeTreatments = (TreatmentsInterface) getTheOneEnabledInArray(pluginsInCategory);
                    if (Config.logConfigBuilder)
                        log.debug("Selected treatment interface: " + ((PluginBase) activeTreatments).getName());
                    for (PluginBase p : pluginsInCategory) {
                        if (!p.getName().equals(((PluginBase) activeTreatments).getName())) {
                            p.setFragmentVisible(false);
                        }
                    }
                    break;
            }

        }
    }

    private PluginBase getTheOneEnabledInArray(ArrayList<PluginBase> pluginsInCategory) {
        PluginBase found = null;
        for (PluginBase p : pluginsInCategory) {
            if (p.isEnabled() && found == null) {
                found = p;
                continue;
            } else if (p.isEnabled()) {
                // set others disabled
                p.setFragmentEnabled(false);
            }
        }
        // If none enabled, enable first one
        if (found == null)
            found = pluginsInCategory.get(0);
        return found;
    }

    private void storeSettings() {
        if (Config.logPrefsChange)
            log.debug("Storing settings");
        SharedPreferences settings = MainApp.instance().getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        for (PluginBase p : pluginList) {
            editor.putBoolean(p.getName() + "Enabled", p.isEnabled());
            editor.putBoolean(p.getName() + "Visible", p.isVisibleInTabs());
        }
        editor.commit();
        verifySelectionInCategories();
    }

    private void loadSettings() {
        if (Config.logPrefsChange)
            log.debug("Loading stored settings");
        SharedPreferences settings = MainApp.instance().getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        for (PluginBase p : pluginList) {
            if (settings.contains(p.getName() + "Enabled"))
                p.setFragmentEnabled(settings.getBoolean(p.getName() + "Enabled", true));
            if (settings.contains(p.getName() + "Visible"))
                p.setFragmentVisible(settings.getBoolean(p.getName() + "Visible", true));
        }
        verifySelectionInCategories();
    }

    /****
     * Method for Setting the Height of the ListView dynamically.
     * *** Hack to fix the issue of not showing all the items of the ListView
     * *** when placed inside a ScrollView
     ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * Constrains interface
     **/
    @Override
    public boolean isAutomaticProcessingEnabled() {
        boolean result = true;

        ArrayList<PluginBase> constrainsPlugins = MainActivity.getSpecificPluginsList(PluginBase.CONSTRAINS);
        for(PluginBase p: constrainsPlugins) {
            ConstrainsInterface constrain = (ConstrainsInterface) p;
            if (!p.isEnabled()) continue;
            result = result && constrain.isAutomaticProcessingEnabled();
        }
        return result;
    }

    @Override
    public boolean manualConfirmationNeeded() {
        boolean result = false;

        ArrayList<PluginBase> constrainsPlugins = MainActivity.getSpecificPluginsList(PluginBase.CONSTRAINS);
        for(PluginBase p: constrainsPlugins) {
            ConstrainsInterface constrain = (ConstrainsInterface) p;
            if (!p.isEnabled()) continue;
            result = result || constrain.manualConfirmationNeeded();
        }
        return result;
    }

    @Override
    public APSResult applyBasalConstrains(APSResult result) {
        ArrayList<PluginBase> constrainsPlugins = MainActivity.getSpecificPluginsList(PluginBase.CONSTRAINS);
        for(PluginBase p: constrainsPlugins) {
            ConstrainsInterface constrain = (ConstrainsInterface) p;
            if (!p.isEnabled()) continue;
            constrain.applyBasalConstrains(result);
        }
        return result;
    }

}
