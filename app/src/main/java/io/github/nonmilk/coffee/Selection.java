package io.github.nonmilk.coffee;

import io.github.shimeoki.jfx.rasterization.Point2i;

public final class Selection {

    private int x;
    private int y;

    private int width;
    private int height;

    public Selection() {
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void update(final Point2i p1, final Point2i p2) {
        final int x1 = p1.x();
        final int x2 = p2.x();

        final int y1 = p1.y();
        final int y2 = p2.y();

        x = Math.min(x1, x2);
        width = Math.abs(x1 - x2);

        y = Math.min(y1, y2);
        height = Math.abs(y1 - y2);
    }
}
