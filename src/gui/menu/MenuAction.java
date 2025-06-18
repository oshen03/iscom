package gui.menu;

/**
 *
 * @author Oshen Sathsara <oshensathsara2003@gmail.com>
 */
public class MenuAction {

    protected boolean isCancel() {
        return cancel;
    }

    public void cancel() {
        this.cancel = true;
    }

    private boolean cancel = false;
}
