package edu.cnu.bopit.app;

import edu.cnu.bopit.BopitVersion;
import edu.cnu.bopit.ui.workbench.BopitWorkbenchView;
import edu.cnu.mdi.app.BaseMDIApplication;
import edu.cnu.mdi.util.PropertyUtils;

/** MDI application shell for the BOPIT scientific workbench. */
public final class BopitApplication extends BaseMDIApplication {
    private static BopitApplication instance;

    private BopitApplication() {
        super(PropertyUtils.TITLE, BopitVersion.APPLICATION_NAME,
                PropertyUtils.FRACTION, 0.85,
                PropertyUtils.CONSOLELOG, true);
    }

    public static BopitApplication getInstance() {
        if (instance == null) instance = new BopitApplication();
        return instance;
    }

    @Override
    protected String getApplicationId() {
        return "bopit-mdi";
    }

    @Override
    protected void addInitialViews() {
        new BopitWorkbenchView();
    }

    /** Launch BOPIT MDI on the Swing event-dispatch thread. */
    public static void main(String[] args) {
        BaseMDIApplication.launch(BopitApplication::getInstance);
    }
}
