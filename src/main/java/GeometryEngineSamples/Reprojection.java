
/*
 * Copyright 2015 Esri.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package GeometryEngineSamples;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.Callout.LeaderPosition;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class Reprojection extends Application {

  private MapView mapView;
  private GraphicsOverlay graphicsOverlay;

  private static final SpatialReference OSGB = SpatialReference.create(27700);

  @Override
  public void start(Stage stage) throws Exception {
    BorderPane stackPane = new BorderPane();
    javafx.scene.Scene scene1 = new javafx.scene.Scene(stackPane);

    stage.setWidth(800);
    stage.setHeight(700);
    stage.setScene(scene1);
    stage.show();

    try {
      ArcGISMap map = new ArcGISMap(Basemap.createNationalGeographic());
      mapView = new MapView();
      mapView.setMap(map);
      mapView.setViewpoint(new Viewpoint(54.37, -3.43, 10000000));

      graphicsOverlay = new GraphicsOverlay();
      mapView.getGraphicsOverlays().add(graphicsOverlay);

      SimpleMarkerSymbol circleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 20);
      mapView.setOnMouseClicked(e -> {
        if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
          graphicsOverlay.getGraphics().clear();

          Point2D point = new Point2D(e.getX(), e.getY());
          Point mapPoint = mapView.screenToLocation(point);
          Point newPoint = new Point(mapPoint.getX(), mapPoint.getY(), mapView.getSpatialReference());
          Graphic graphic = new Graphic(newPoint, circleSymbol);
          graphicsOverlay.getGraphics().add(graphic);

          displayOSGBCoordinates(newPoint);
        }
      });

      stackPane.setCenter(mapView);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Reprojects x,y  coordinates from Point passed to the OSGB coordinates system.
   * <p>
   * Displays new coordinates on MapView using a Callout.
   * 
   * @param point 
   */
  public void displayOSGBCoordinates(Point point) {
    Point reprojectedGeometry = (Point) GeometryEngine.project(point, OSGB);
    String coordinates = "X: " + reprojectedGeometry.getX() + "\n" + "Y: " + reprojectedGeometry.getY();

    Callout callout = mapView.getCallout();
    callout.setVisible(true);
    callout.setStyle("-fx-font-size: 24;");
    callout.setMargin(10);
    callout.setTitle("OSGB Coordinates");
    callout.showCalloutAt(reprojectedGeometry);
    callout.setLeaderPosition(LeaderPosition.BOTTOM);
    callout.setDetail(coordinates);
  }

  public void stop() throws Exception {
    if (mapView != null) {
      mapView.dispose();
    }
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}
