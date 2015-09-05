package org.jdownloader.controlling.contextmenu;

import java.awt.AlphaComposite;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.appwork.swing.action.BasicAction;
import org.appwork.swing.components.CheckBoxIcon;
import org.appwork.utils.GetterSetter;
import org.appwork.utils.ReflectionUtils;
import org.appwork.utils.StringUtils;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.reflection.Clazz;
import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.views.downloads.action.Modifier;
import org.jdownloader.gui.views.linkgrabber.bottombar.IncludedSelectionSetup;
import org.jdownloader.images.NewTheme;

public abstract class CustomizableAppAction extends AppAction {
    private MenuItemData menuItemData;

    public MenuItemData getMenuItemData() {
        return menuItemData;
    }

    private HashSet<ActionContext> setupObjects;

    public List<ActionContext> getSetupObjects() {
        if (setupObjects != null) {
            return new ArrayList<ActionContext>(setupObjects);
        }
        return null;
    }

    protected static ImageIcon getCheckBoxedIcon(String string, boolean selected, boolean enabled) {
        return new ImageIcon(ImageProvider.merge(NewTheme.I().getIcon(string, 18), new CheckBoxIcon(selected, enabled), -2, -2, 6, 6, null, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f)));
    }

    private long lastRequestUpdate;

    public void removeContextSetup(ActionContext contextSetup) {
        if (setupObjects != null) {
            this.setupObjects.remove(contextSetup);
        }
    }

    public void addContextSetup(ActionContext contextSetup) {
        if (this.setupObjects == null) {
            this.setupObjects = new HashSet<ActionContext>();
        }
        this.setupObjects.add(contextSetup);

    }

    public void loadContextSetups() {
        if (setupObjects != null) {
            fill(setupObjects);
        }
    }

    /**
     * @param setupObjects2
     */
    private void fill(HashSet<ActionContext> setupObjects2) {
        if (setupObjects2 != null && menuItemData != null) {
            for (ActionContext setupObject : setupObjects2) {
                for (GetterSetter f : ReflectionUtils.getGettersSetteres(setupObject.getClass())) {
                    try {
                        if (f.getAnnotation(Customizer.class) != null) {
                            Object v = menuItemData.getActionData().fetchSetup(f.getKey());
                            if (v == null) {
                                continue;
                            }
                            if (Clazz.isEnum(f.getType())) {

                                v = ReflectionUtils.getEnumValueOf((Class<? extends Enum>) f.getType(), v.toString());
                                if (v == null) {
                                    continue;
                                }
                            }
                            if (f.getType() == Modifier.class) {
                                v = Modifier.create((String) v);

                            }
                            f.set(setupObject, v);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (setupObject instanceof IncludedSelectionSetup) {
                    ((IncludedSelectionSetup) setupObject).updateListeners();
                }
            }
        }
    }

    public void requestUpdate(Object requestor) {
        lastRequestUpdate = System.currentTimeMillis();
        fill(setupObjects);

    }

    @Override
    public void setName(String name) {
        String actualName = name;
        if (menuItemData != null) {
            ActionData actionData = menuItemData.getActionData();
            if (StringUtils.isNotEmpty(actionData.getName())) {
                name = actionData.getName();
            }
            if (StringUtils.isNotEmpty(menuItemData.getName())) {
                name = menuItemData.getName();
            }
        }
        if (MenuItemData.isEmptyValue(name)) {
            name = "";
        }
        if (StringUtils.isEmpty(actualName)) {
            actualName = getName();
        }
        if (StringUtils.isEmpty(getTooltipText()) && StringUtils.isEmpty(name)) {
            if (StringUtils.isNotEmpty(actualName)) {
                setTooltipText(actualName);
            }
        }

        super.setName(name);
    }

    @Override
    public BasicAction setAccelerator(KeyStroke stroke) {
        if (menuItemData != null) {
            if (StringUtils.isNotEmpty(menuItemData.getShortcut())) {
                stroke = KeyStroke.getKeyStroke(menuItemData.getShortcut());
            }
        }

        return super.setAccelerator(stroke);
    }

    public CustomizableAppAction() {
        super();
        if (this instanceof ActionContext) {
            addContextSetup((ActionContext) this);
        }
    }

    public void initContextDefaults() {

    }

    @Override
    public Object getValue(String key) {
        if (Action.MNEMONIC_KEY == key) {
            if (System.currentTimeMillis() - lastRequestUpdate > 1000) {
                System.out.println("Bad Action Usage!");
                new Exception().printStackTrace();
                requestUpdate(null);
            }

        }

        if (iconKey != null && LARGE_ICON_KEY.equalsIgnoreCase(key)) {
            if (MenuItemData.isEmptyValue(iconKey)) {
                return null;
            }
            return NewTheme.I().getIcon(iconKey, size);
        } else if (iconKey != null && SMALL_ICON.equalsIgnoreCase(key)) {
            if (MenuItemData.isEmptyValue(iconKey)) {
                return null;
            }
            return NewTheme.I().getIcon(iconKey, size);
        }
        return super.getValue(key);
    }

    public void setMenuItemData(MenuItemData data) {
        this.menuItemData = data;
        fill(setupObjects);

    }

    @Override
    public void setIconKey(String iconKey) {

        if (menuItemData != null) {
            ActionData actionData = menuItemData.getActionData();
            if (StringUtils.isNotEmpty(actionData.getIconKey())) {
                iconKey = actionData.getIconKey();
            }
            if (StringUtils.isNotEmpty(menuItemData.getIconKey())) {
                iconKey = menuItemData.getIconKey();

            }
        }

        super.setIconKey(iconKey);
    }

    /**
     *
     */
    public void applyMenuItemData() {
        if (menuItemData == null) {
            return;
        }
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                // the setters read property from the menuItem Data backend. this setName(getName()) makes sense and is NO bug!

                setName(getName());
                setIconKey(getIconKey());
                setAccelerator(null);

            }
        }.getReturnValue();

    }

    public List<KeyStroke> getAdditionalShortcuts(KeyStroke keystroke) {
        return null;
    }

}
