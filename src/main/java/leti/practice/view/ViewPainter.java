package leti.practice.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import leti.practice.structures.graph.EdgeProperties;
import leti.practice.structures.graph.Node;
import leti.practice.structures.graph.ResidualNetwork;

import java.util.HashMap;

public abstract class ViewPainter {
    private Canvas canvas = null;
    private final double nodeSize = 34;
    protected HashMap<Node, NodeViewParameters> nodeViewParameters;
    protected boolean needRecalculateNodesParameters = true;

    public abstract void paint(ResidualNetwork<Double> network);
    protected abstract void calculateNodesParameters(ResidualNetwork<Double> network);

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(new Font("Arial", 10));
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public boolean isCanvasSet() {
        return canvas != null;
    }

    public void expandCanvas(double x, double y, double size) {
        double additionalMargin = 100;

        if (canvas.getWidth() - (x + size) < 0) {
            canvas.setWidth(x + size + additionalMargin);
        }

        if (canvas.getHeight() - (y + size) < 0) {
            canvas.setHeight(y + size + additionalMargin);
        }
    }

    public void clearCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void setNeedRecalculateNodesParameters(boolean value) {
        needRecalculateNodesParameters = value;
    }

    public boolean setNodePosition(String nodeName, double x, double y) {
        Node node = new Node(nodeName);

        if (nodeViewParameters.containsKey(node)) {
            NodeViewParameters nodeParams = nodeViewParameters.get(node);
            nodeParams.x = x;
            nodeParams.y = y;
            return true;
        }

        return false;
    }

    public String getNodeNameByPosition(double x, double y) {
        for (Node node : nodeViewParameters.keySet()) {
            NodeViewParameters nodeParams = nodeViewParameters.get(node);

            if (Math.abs(nodeParams.x - x) <= nodeSize && Math.abs(nodeParams.y - y) <= nodeSize) {
                return node.getName();
            }
        }

        return null;
    }

    public void paintNode(double x, double y, String name) {
        paintNode(x, y, name, Color.BLACK);
    }

    public void paintNode(double x, double y, String name, Paint color) {
        expandCanvas(x, y, nodeSize);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        Paint originalPaint = gc.getStroke();

        gc.setStroke(color);
        gc.setFill(Color.WHITE);
        gc.fillOval(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);
        gc.setFill(Color.BLACK);
        gc.strokeOval(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);
        gc.fillText(name, x, y);
        gc.setStroke(originalPaint);
    }

    public void paintEdge(double sx, double sy, double dx, double dy, String info, LineType lineType) {
        paintEdge(sx, sy, dx, dy, info, lineType, Color.BLACK);
    }

    public void paintEdge(double sx, double sy, double dx, double dy, String info, LineType lineType, Paint color) {
        expandCanvas(Math.max(sx, dx), Math.max(sy, dy), 0);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        double indent = nodeSize / 4;
        double distanceX = Math.abs(dx - sx);
        double distanceY = Math.abs(dy - sy);
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        double sign_1 = (dy > sy)? -1 : 1;
        double sign_2 = (sx > dx)? -1 : 1;
        double deltaX = sign_1 * distanceY * indent / distance;
        double deltaY = sign_2 * distanceX * indent / distance;

        double new_sx, new_dx, new_sy, new_dy;

        if (lineType == LineType.STRAIGHT) {
             new_sx = sx + deltaX;
             new_dx = dx + deltaX;
             new_sy = sy + deltaY;
             new_dy = dy + deltaY;
        } else {
             new_sx = sx - deltaX;
             new_dx = dx - deltaX;
             new_sy = sy - deltaY;
             new_dy = dy - deltaY;
        }

        double centerX = (Math.max(new_dx, new_sx) + Math.min(new_dx, new_sx)) / 2;
        double centerY = (Math.max(new_dy, new_sy) + Math.min(new_dy, new_sy)) / 2;

        double arcCurvature = 6;
        double arrowRatio = 0.5;

        Paint originalPaint = gc.getStroke();
        gc.setStroke(color);

        if (lineType == LineType.STRAIGHT) {
            gc.strokeLine(new_sx, new_sy, new_dx, new_dy);
            gc.strokeLine(centerX, centerY,
                    centerX + arrowRatio * (deltaX - deltaY), centerY + arrowRatio * (deltaY + deltaX));
            gc.strokeLine(centerX, centerY,
                    centerX + arrowRatio * (-deltaX - deltaY), centerY + arrowRatio * (-deltaY + deltaX));
            paintText(centerX + deltaX, centerY + deltaY, info, Color.RED);
        } else if (lineType == LineType.ARC) {
            gc.setLineDashes(5);
            gc.beginPath();
            gc.moveTo(new_sx, new_sy);
            gc.quadraticCurveTo(centerX - deltaX * arcCurvature, centerY - deltaY * arcCurvature, new_dx, new_dy);
            gc.stroke();
            gc.setLineDashes(0);

            double arcCenterX = centerX - 3 * deltaX;
            double arcCenterY = centerY - 3 * deltaY;

            gc.strokeLine(arcCenterX, arcCenterY,
                    arcCenterX + arrowRatio * (deltaX - deltaY), arcCenterY + arrowRatio * (deltaY + deltaX));
            gc.strokeLine(arcCenterX, arcCenterY, arcCenterX + arrowRatio * (-deltaX - deltaY),
                    arcCenterY + arrowRatio * (-deltaY + deltaX));
            paintText(centerX - deltaX * (arcCurvature - 1), centerY - deltaY * (arcCurvature - 1), info, Color.RED);
        }

        gc.setStroke(originalPaint);
    }

    public void paintText(double x, double y, String text) {
        paintText(x, y, text, Color.BLACK);
    }

    public void paintText(double x, double y, String text, Paint paint) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Paint originalPaint = gc.getFill();
        gc.setFill(paint);
        gc.fillText(text, x, y);
        gc.setFill(originalPaint);
    }

    protected void paintEdges(ResidualNetwork<Double> network, boolean paintReverseEdges) {
        for (Node source : network.getNetworkNodes()) {
            HashMap<Node, EdgeProperties<Double>> edges = network.getNetworkEdges(source);

            if (edges != null) {
                for (Node destination : edges.keySet()) {
                    NodeViewParameters sourceParams = nodeViewParameters.get(source);
                    NodeViewParameters destParams = nodeViewParameters.get(destination);
                    EdgeProperties<Double> edgeParams = edges.get(destination);
                    String info = edgeParams.getFlow() + "/" + edgeParams.getCapacity();

                    if (sourceParams == null || destParams == null) {
                        continue;
                    }

                    if (edgeParams.getFlow() > 0) {
                        paintEdge(sourceParams.x, sourceParams.y, destParams.x, destParams.y, info,
                                LineType.STRAIGHT, Color.BLUE);
                    } else {
                        paintEdge(sourceParams.x, sourceParams.y, destParams.x, destParams.y, info, LineType.STRAIGHT);
                    }
                }
            }
        }

        if (!paintReverseEdges) {
            return;
        }

        for (Node source : network.getReverseNetworkNodes()) {
            HashMap<Node, EdgeProperties<Double>> edges = network.getReverseNetworkEdges(source);

            if (edges != null) {
                for (Node destination : edges.keySet()) {
                    NodeViewParameters sourceParams = nodeViewParameters.get(source);
                    NodeViewParameters destParams = nodeViewParameters.get(destination);
                    EdgeProperties<Double> edgeParams = edges.get(destination);
                    String info = edgeParams.getFlow() + "/" + edgeParams.getCapacity();

                    if (sourceParams == null || destParams == null) {
                        continue;
                    }

                    paintEdge(sourceParams.x, sourceParams.y, destParams.x, destParams.y, info, LineType.ARC);
                }
            }
        }
    }

    protected void paintNodes(ResidualNetwork<Double> network) {
        for (NodeViewParameters parameters : nodeViewParameters.values()) {
            Double height = network.getSurpluses().get(parameters.node);
            Paint color = Color.BLACK;

            if (network.getSource() != null && network.getSource().equals(parameters.node)) {
                color = Color.GREEN;
            } else if (network.getDestination() != null && network.getDestination().equals(parameters.node)) {
                color = Color.RED;
            } else if (height != null && height > 0) {
                color = Color.BLUE;
            }

            paintNode(parameters.x, parameters.y, parameters.node.getName(), color);
        }
    }
}
