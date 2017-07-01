package ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Belal on 2/3/2016.
 */
//Extending FragmentStatePagerAdapter
public class Pager extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    int tabCount;

    //Constructor to the class
    public Pager(FragmentManager fm, int tabCount) {
        super(fm);
        //Initializing tab count
        this.tabCount= tabCount;
    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                ContactArtifects contactArtifectsTab = new ContactArtifects();
                return contactArtifectsTab;
            case 1:
                ImageArtifects imageArtifects = new ImageArtifects();
                return imageArtifects;
            case 2:
                DocumentArtifect documentartifect = new DocumentArtifect();
                return documentartifect;
            default:
                return null;
        }
    }
    @Override
    public CharSequence getPageTitle(int position) {
        super.getPageTitle(position);

        switch (position){
            case 0:
                return "Contacts";
            case 1:
                return "Images";
            case 2:
                return "Documents";

            default:
                return null;
        }
    }
    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }
}