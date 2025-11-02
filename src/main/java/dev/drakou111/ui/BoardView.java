package dev.drakou111.ui;

import dev.drakou111.blocks.*;
import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.Button;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.BlockType;
import dev.drakou111.logic.Board;
import dev.drakou111.logic.Constants;
import dev.drakou111.ui.undo.BoardAction;
import dev.drakou111.ui.undo.CompoundAction;
import dev.drakou111.util.SpriteManager;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class BoardView {
    private static final double BASE_TILE = 32; // base logical tile pixel size

    private final Board board;
    private final SpriteManager sprites;
    private final Canvas canvas;
    // keys
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    // undo/redo
    private final Deque<BoardAction> undoStack = new ArrayDeque<>();
    private final Deque<BoardAction> redoStack = new ArrayDeque<>();
    // camera
    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 4;
    // input
    private boolean middleDragging = false;
    private double dragLastX, dragLastY;
    private boolean primaryDown = false;
    private boolean dragPlacing = false;
    private int pressTx = -1, pressTy = -1;
    private int lastHoverTx = -1, lastHoverTy = -1;
    private boolean rightDown = false;
    private boolean rightDragDeleting = false;
    private int rightPressTx = -1, rightPressTy = -1;
    private int rightLastHoverTx = -1, rightLastHoverTy = -1;
    private double lastMouseX = 0, lastMouseY = 0;
    private boolean wasHoldingSomething = false;
    // callbacks (now instance-based)
    private Consumer<Block> onPickedUp = b -> {
    };
    private Consumer<Block> onHeldBlockChanged = b -> {
    };
    private Block heldBlockPrototype = null;
    private Consumer<Block> onInspect = b -> {
    };
    private CompoundAction currentCompound = null;

    public BoardView(Board board, SpriteManager sprites) {
        this.board = board;
        this.sprites = sprites;
        this.canvas = new Canvas(800, 700);

        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        canvas.setOnMouseEntered(e -> canvas.requestFocus());

        // center initial camera on board
        offsetX = (canvas.getWidth() - board.getWidth() * BASE_TILE * scale) / 2.0;
        offsetY = (canvas.getHeight() - board.getHeight() * BASE_TILE * scale) / 2.0;

        setupInput();
        startRenderLoop();
    }

    public Node getNode() {
        return canvas;
    }

    private void setupInput() {
        canvas.setOnMousePressed(e -> {

            currentCompound = new CompoundAction();

            if (e.getButton() == MouseButton.MIDDLE) {
                middleDragging = true;
                dragLastX = e.getX();
                dragLastY = e.getY();
            } else if (e.getButton() == MouseButton.PRIMARY) {
                primaryDown = true;
                double worldX = (e.getX() - offsetX) / scale;
                double worldY = (e.getY() - offsetY) / scale;
                pressTx = (int) Math.floor(worldX / BASE_TILE);
                pressTy = (int) Math.floor(worldY / BASE_TILE);
                lastHoverTx = pressTx;
                lastHoverTy = pressTy;
                dragPlacing = false;
            } else if (e.getButton() == MouseButton.SECONDARY) {
                rightDown = true;
                double worldX = (e.getX() - offsetX) / scale;
                double worldY = (e.getY() - offsetY) / scale;
                rightPressTx = (int) Math.floor(worldX / BASE_TILE);
                rightPressTy = (int) Math.floor(worldY / BASE_TILE);
                rightLastHoverTx = rightPressTx;
                rightLastHoverTy = rightPressTy;
                rightDragDeleting = false;

                heldBlockPrototype = null;
                onHeldBlockChanged.accept(null);
                onPickedUp.accept(null);
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (currentCompound != null && !currentCompound.isEmpty()) {
                undoStack.push(currentCompound);
                redoStack.clear();
            }
            currentCompound = null;

            if (e.getButton() == MouseButton.MIDDLE) {
                // existing middle-mouse pickup logic (leave unchanged)
                if (middleDragging) {
                    double worldX = (e.getX() - offsetX) / scale;
                    double worldY = (e.getY() - offsetY) / scale;
                    int tx = (int) Math.floor(worldX / BASE_TILE);
                    int ty = (int) Math.floor(worldY / BASE_TILE);
                    if (tx >= 0 && ty >= 0 && tx < board.getWidth() && ty < board.getHeight()) {
                        Block b = board.getBlockAt(tx, ty);
                        if (b != null && b.getBlockType() != BlockType.AIR) {
                            try {
                                Block proto = b.clone();
                                heldBlockPrototype = proto;
                                onHeldBlockChanged.accept(proto);
                                onPickedUp.accept(proto);
                                wasHoldingSomething = true;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                middleDragging = false;

            } else if (e.getButton() == MouseButton.PRIMARY) {
                if (!dragPlacing) {
                    if (pressTx >= 0 && pressTy >= 0 && pressTx < board.getWidth() && pressTy < board.getHeight()) {

                        if (heldBlockPrototype == null) {
                            // Inspect the block under the cursor
                            Block clickedBlock = board.getBlockAt(pressTx, pressTy);
                            if (clickedBlock != null) {
                                if (clickedBlock instanceof Button button)
                                    button.press(board);
                                else if (clickedBlock instanceof Lever lever)
                                    lever.toggle(board);
                            }
                        } else {
                            // Normal place logic
                            tryPlace(pressTx, pressTy);
                        }
                    }
                }
                primaryDown = false;
                dragPlacing = false;
                pressTx = pressTy = -1;
                lastHoverTx = lastHoverTy = -1;

            } else if (e.getButton() == MouseButton.SECONDARY) {
                if (!rightDragDeleting && !wasHoldingSomething) {
                    if (rightPressTx >= 0 && rightPressTy >= 0 && rightPressTx < board.getWidth() && rightPressTy < board.getHeight()) {
                        tryDelete(rightPressTx, rightPressTy);
                    }
                }
                rightDown = false;
                rightDragDeleting = false;
                rightPressTx = rightPressTy = -1;
                rightLastHoverTx = rightLastHoverTy = -1;
                wasHoldingSomething = false;
            }
        });

        canvas.setOnMouseDragged(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();

            // middle dragging camera pan
            if (middleDragging) {
                double dx = e.getX() - dragLastX;
                double dy = e.getY() - dragLastY;
                offsetX += dx;
                offsetY += dy;
                dragLastX = e.getX();
                dragLastY = e.getY();
                return;
            }

            // primary drag: painting (existing logic)
            if (primaryDown) {
                double worldX = (e.getX() - offsetX) / scale;
                double worldY = (e.getY() - offsetY) / scale;
                int tx = (int) Math.floor(worldX / BASE_TILE);
                int ty = (int) Math.floor(worldY / BASE_TILE);

                if (tx >= 0 && ty >= 0 && tx < board.getWidth() && ty < board.getHeight()) {
                    if (!dragPlacing) {
                        if (tx != pressTx || ty != pressTy) {
                            dragPlacing = true;
                            tryPlace(pressTx, pressTy);
                            if (tx != lastHoverTx || ty != lastHoverTy) {
                                tryPlace(tx, ty);
                                lastHoverTx = tx;
                                lastHoverTy = ty;
                            }
                        }
                    } else {
                        if (tx != lastHoverTx || ty != lastHoverTy) {
                            tryPlace(tx, ty);
                            lastHoverTx = tx;
                            lastHoverTy = ty;
                        }
                    }
                }
            }

            // secondary drag: deleting (mirror logic)
            if (rightDown) {
                double worldX = (e.getX() - offsetX) / scale;
                double worldY = (e.getY() - offsetY) / scale;
                int tx = (int) Math.floor(worldX / BASE_TILE);
                int ty = (int) Math.floor(worldY / BASE_TILE);

                if (tx >= 0 && ty >= 0 && tx < board.getWidth() && ty < board.getHeight()) {
                    if (!rightDragDeleting) {
                        if (tx != rightPressTx || ty != rightPressTy) {
                            rightDragDeleting = true;
                            tryDelete(rightPressTx, rightPressTy);
                            if (tx != rightLastHoverTx || ty != rightLastHoverTy) {
                                tryDelete(tx, ty);
                                rightLastHoverTx = tx;
                                rightLastHoverTy = ty;
                            }
                        }
                    } else {
                        if (tx != rightLastHoverTx || ty != rightLastHoverTy) {
                            tryDelete(tx, ty);
                            rightLastHoverTx = tx;
                            rightLastHoverTy = ty;
                        }
                    }
                }
            }
        });

        // also handle mouse moved so that if dragPlacing is true we still place on hover events
        canvas.setOnMouseMoved(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();

            // left dragging hover placement
            if (primaryDown && dragPlacing) {
                double worldX = (e.getX() - offsetX) / scale;
                double worldY = (e.getY() - offsetY) / scale;
                int tx = (int) Math.floor(worldX / BASE_TILE);
                int ty = (int) Math.floor(worldY / BASE_TILE);
                if (tx >= 0 && ty >= 0 && tx < board.getWidth() && ty < board.getHeight()) {
                    if (tx != lastHoverTx || ty != lastHoverTy) {
                        tryPlace(tx, ty);
                        lastHoverTx = tx;
                        lastHoverTy = ty;
                    }
                }
            }

            // right dragging hover deletion
            if (rightDown && rightDragDeleting) {
                double worldX = (e.getX() - offsetX) / scale;
                double worldY = (e.getY() - offsetY) / scale;
                int tx = (int) Math.floor(worldX / BASE_TILE);
                int ty = (int) Math.floor(worldY / BASE_TILE);
                if (tx >= 0 && ty >= 0 && tx < board.getWidth() && ty < board.getHeight()) {
                    if (tx != rightLastHoverTx || ty != rightLastHoverTy) {
                        tryDelete(tx, ty);
                        rightLastHoverTx = tx;
                        rightLastHoverTy = ty;
                    }
                }
            }
        });

        // zoom and keyboard unchanged
        canvas.addEventHandler(ScrollEvent.SCROLL, e -> {

            if (e.isControlDown()) {
                if (heldBlockPrototype instanceof PoweredBlock poweredBlock) {
                    int power = poweredBlock.getPower();
                    int delta = (int) Math.signum(e.getDeltaY());

                    // Increment or decrement with wrap
                    if (delta > 0) {
                        power = wrapPower(power + 1);
                    } else if (delta < 0) {
                        power = wrapPower(power - 1);
                    }

                    // Clone and set new power once
                    heldBlockPrototype = poweredBlock.clone();
                    ((PoweredBlock) heldBlockPrototype).setPower(power);
                }
            } else {
                double zoomFactor = Math.pow(1.0015, e.getDeltaY());
                double mouseX = e.getX();
                double mouseY = e.getY();

                double worldXBefore = (mouseX - offsetX) / scale;
                double worldYBefore = (mouseY - offsetY) / scale;

                scale *= zoomFactor;
                scale = Math.max(0.2, Math.min(4.0, scale));

                double worldXAfter = (mouseX - offsetX) / scale;
                double worldYAfter = (mouseY - offsetY) / scale;

                offsetX += (worldXAfter - worldXBefore) * scale;
                offsetY += (worldYAfter - worldYBefore) * scale;
            }
            e.consume();
        });

        canvas.setFocusTraversable(true);

        canvas.setOnKeyPressed(e -> {
            pressedKeys.add(e.getCode());
            KeyCode code = e.getCode();

            if (e.isControlDown() && e.getCode() == KeyCode.Z) undo();
            else if (e.isControlDown() && e.getCode() == KeyCode.Y) redo();

            if (heldBlockPrototype == null)
                return;

            boolean shift = e.isShiftDown();

            if (code == KeyCode.E || (code == KeyCode.R && !shift)) {
                // Clockwise rotation
                heldBlockPrototype = heldBlockPrototype.clone();
                heldBlockPrototype.rotateClockwise();
                onHeldBlockChanged.accept(heldBlockPrototype);
                onPickedUp.accept(heldBlockPrototype);
            } else if (code == KeyCode.Q || (code == KeyCode.R && shift)) {
                // Counterclockwise rotation
                heldBlockPrototype = heldBlockPrototype.clone();
                heldBlockPrototype.rotateCounterClockwise();
                onHeldBlockChanged.accept(heldBlockPrototype);
                onPickedUp.accept(heldBlockPrototype);
            } else if (code == KeyCode.SPACE) {
                switch (heldBlockPrototype) {
                    case Repeater repeater -> {
                        int delay = repeater.getDelay();
                        delay++;
                        if (delay > Constants.REPEATER_MAX_DELAY)
                            delay = Constants.REPEATER_MIN_DELAY;
                        heldBlockPrototype = new Repeater(repeater.getDirection(), delay);
                    }
                    case Comparator comparator ->
                            heldBlockPrototype = new Comparator(comparator.getDirection(), !comparator.isSubtractMode());
                    case Dust dust -> heldBlockPrototype = new Dust(!dust.getShouldBeDot());
                    case CopperBulb bulb -> {
                        heldBlockPrototype = new CopperBulb();
                        ((CopperBulb) heldBlockPrototype).setLit(!bulb.isLit());
                    }
                    case RedstoneLamp lamp -> {
                        heldBlockPrototype = new RedstoneLamp();
                        ((RedstoneLamp) heldBlockPrototype).setLit(!lamp.isLit());
                    }
                    default -> {
                    }
                }
                onHeldBlockChanged.accept(heldBlockPrototype);
                onPickedUp.accept(heldBlockPrototype);
            }
        });

        canvas.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));
    }

    private void startRenderLoop() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double panSpeed = 3;
                if (pressedKeys.contains(KeyCode.W)) offsetY += panSpeed;
                if (pressedKeys.contains(KeyCode.S)) offsetY -= panSpeed;
                if (pressedKeys.contains(KeyCode.A)) offsetX += panSpeed;
                if (pressedKeys.contains(KeyCode.D)) offsetX -= panSpeed;
                render(gc);
            }
        };
        loop.start();
    }

    private void render(GraphicsContext g) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        g.setFill(Color.web("#f0f0f0"));
        g.fillRect(0, 0, w, h);

        g.save();
        g.translate(offsetX, offsetY);
        g.scale(scale, scale);

        // draw background grid
        double bx = 0, by = 0;
        double gridW = board.getWidth() * BASE_TILE;
        double gridH = board.getHeight() * BASE_TILE;

        g.setFill(Color.WHITE);
        g.fillRect(bx, by, gridW, gridH);

        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(1.0 / scale);
        for (int i = 0; i <= board.getWidth(); i++) {
            double x = i * BASE_TILE;
            g.strokeLine(x, 0, x, gridH);
        }
        for (int j = 0; j <= board.getHeight(); j++) {
            double y = j * BASE_TILE;
            g.strokeLine(0, y, gridW, y);
        }

        // draw blocks visible in viewport
        // compute visible tile range
        double invScale = 1.0 / scale;
        double viewLeft = -offsetX * invScale;
        double viewTop = -offsetY * invScale;
        double viewRight = viewLeft + canvas.getWidth() * invScale;
        double viewBottom = viewTop + canvas.getHeight() * invScale;

        int minTx = (int) Math.floor(viewLeft / BASE_TILE) - 1;
        int minTy = (int) Math.floor(viewTop / BASE_TILE) - 1;
        int maxTx = (int) Math.ceil(viewRight / BASE_TILE) + 1;
        int maxTy = (int) Math.ceil(viewBottom / BASE_TILE) + 1;

        minTx = Math.max(0, minTx);
        minTy = Math.max(0, minTy);
        maxTx = Math.min(board.getWidth() - 1, maxTx);
        maxTy = Math.min(board.getHeight() - 1, maxTy);

        for (int y = minTy; y <= maxTy; y++) {
            for (int x = minTx; x <= maxTx; x++) {
                Block b = board.getBlockAt(x, y);
                if (b != null) {
                    if (b.getSpriteKey() == null) continue;
                    var img = sprites.get(b.getSpriteKey());
                    if (img != null) {
                        g.setImageSmoothing(false);
                        g.drawImage(img, x * BASE_TILE, y * BASE_TILE, BASE_TILE, BASE_TILE);
                    } else {
                        // fallback rectangle
                        g.setFill(Color.DARKGRAY);
                        g.fillRect(x * BASE_TILE + 2, y * BASE_TILE + 2, BASE_TILE - 4, BASE_TILE - 4);
                    }
                }
            }
        }

        // draw ghost held block under cursor (placeholder)
        if (heldBlockPrototype != null) {
            var img = sprites.get(heldBlockPrototype.getSpriteKey());
            if (img != null) {
                double worldX = (lastMouseX - offsetX) / scale;
                double worldY = (lastMouseY - offsetY) / scale;

                int tx = (int) Math.floor(worldX / BASE_TILE);
                int ty = (int) Math.floor(worldY / BASE_TILE);

                // draw only if within bounds
                if (tx >= 0 && ty >= 0 && tx < board.getWidth() && ty < board.getHeight()) {
                    double drawX = tx * BASE_TILE;
                    double drawY = ty * BASE_TILE;

                    g.setGlobalAlpha(0.5);
                    g.setImageSmoothing(false);
                    g.drawImage(img, drawX, drawY, BASE_TILE, BASE_TILE);
                    g.setGlobalAlpha(1.0);
                }
            }
        }

        g.restore();
    }

    public void setHeldBlockPrototype(Block prototype) {
        this.heldBlockPrototype = prototype;
        if (prototype != null)
            wasHoldingSomething = true;
        onHeldBlockChanged.accept(prototype);
    }

    public void setOnInspect(Consumer<Block> c) {
        this.onInspect = c;
    }

    public void setOnPickedUp(Consumer<Block> c) {
        this.onPickedUp = c;
    }

    public void setOnHeldBlockChanged(Consumer<Block> c) {
        this.onHeldBlockChanged = c;
    }

    private void tryPlace(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= board.getWidth() || ty >= board.getHeight()) return;
        if (heldBlockPrototype == null || heldBlockPrototype.getBlockType() == BlockType.AIR) return;
        if (board.getBlockAt(tx, ty).getBlockType() != BlockType.AIR && !pressedKeys.contains(KeyCode.SHIFT)) return;

        Block newBlock = heldBlockPrototype.clone();
        if (!newBlock.canPlaceAt(board, tx, ty)) return;

        board.setBlockAtWithUndo(newBlock, tx, ty, currentCompound, undoStack, redoStack);
    }

    private void tryDelete(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= board.getWidth() || ty >= board.getHeight()) return;
        Block current = board.getBlockAt(tx, ty);
        if (current == null || current.getBlockType() == BlockType.AIR) return;

        board.setBlockAtWithUndo(new Air(), tx, ty, currentCompound, undoStack, redoStack);
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            BoardAction action = undoStack.pop();
            action.undo(board);
            redoStack.push(action);
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            BoardAction action = redoStack.pop();
            action.redo(board);
            undoStack.push(action);
        }
    }

    private int wrapPower(int power) {
        if (power > Constants.MAX_POWER) return Constants.MIN_POWER;
        if (power < Constants.MIN_POWER) return Constants.MAX_POWER;
        return power;
    }
}
