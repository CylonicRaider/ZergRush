package net.zergrush.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;

public class SquareLayout implements LayoutManager2 {

    private Component child;

    public String toString() {
        return getClass().getName() + "[]";
    }

    private void validateConstraints(Object constraints) {
        if (constraints != null)
            throw new IllegalArgumentException("SquareLayout constraints " +
                "must be null");
    }

    public void addLayoutComponent(String name, Component comp) {
        validateConstraints(name);
        if (child != null)
            throw new IllegalStateException("Trying to add child to " +
                "a SquareLayout that already has a child");
        child = comp;
    }

    public void addLayoutComponent(Component comp, Object constraints) {
        validateConstraints(constraints);
        addLayoutComponent((String) null, comp);
    }

    public void removeLayoutComponent(Component comp) {
        if (comp == child) child = null;
    }

    public Component getLayoutComponent(Object constraints) {
        validateConstraints(constraints);
        return child;
    }

    public Object getConstraints(Component child) {
        return null;
    }

    public Dimension minimumLayoutSize(Container parent) {
        Dimension d = (child == null) ? null : child.getMinimumSize();
        return inflateContainer(d, parent);
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension d = (child == null) ? null : child.getPreferredSize();
        return inflateContainer(d, parent);
    }

    public Dimension maximumLayoutSize(Container parent) {
        Dimension d = (child == null) ? null : child.getMaximumSize();
        return inflateContainer(d, parent);
    }

    public float getLayoutAlignmentX(Container target) {
        return Component.CENTER_ALIGNMENT;
    }

    public float getLayoutAlignmentY(Container target) {
        return Component.CENTER_ALIGNMENT;
    }

    public void layoutContainer(Container target) {
        if (child == null) return;
        Rectangle bounds = target.getBounds();
        deflateIP(bounds, target.getInsets());
        int size = Math.min(bounds.width, bounds.height);
        int offsetX = (bounds.width - size) / 2;
        int offsetY = (bounds.height - size) / 2;
        child.setBounds(bounds.x + offsetX, bounds.y + offsetY, size, size);
    }

    public void invalidateLayout(Container target) {
        /* We do not cache anything */
    }

    private Dimension inflateContainer(Dimension dim, Container parent) {
        if (dim == null)
            dim = new Dimension();
        if (parent != null) {
            Insets ins = parent.getInsets();
            dim.width += ins.left + ins.right;
            dim.height = ins.top + ins.bottom;
        }
        return dim;
    }

    private static void deflateIP(Rectangle rect, Insets ins) {
        rect.x += ins.left;
        rect.y += ins.top;
        rect.width -= ins.left + ins.right;
        rect.height -= ins.top + ins.bottom;
    }

}
