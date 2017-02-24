
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
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;

public class CrossPolygon extends Application {

  private MapView mapView;
  private GraphicsOverlay polylineGraphicsOverlay;
  private PolygonBuilder polygonBuilder;
  private PolylineBuilder polylineBuilder;
  private SimpleFillSymbol fillSymbol;
  private int countPoints = 0;
  private static final SpatialReference WEB_MERCATOR = SpatialReferences.getWebMercator();

  @Override
  public void start(Stage stage) throws Exception {
    BorderPane stackPane = new BorderPane();
    javafx.scene.Scene scene1 = new javafx.scene.Scene(stackPane);

    stage.setWidth(800);
    stage.setHeight(700);
    stage.setScene(scene1);
    stage.show();

    try {
      ArcGISMap map = new ArcGISMap(Basemap.createImagery());
      mapView = new MapView();
      mapView.setMap(map);
      mapView.setViewpoint(new Viewpoint(39.623119, -107.635053, 3000));

      GraphicsOverlay polygonGraphicsOverlay = new GraphicsOverlay();
      polylineGraphicsOverlay = new GraphicsOverlay();
      mapView.getGraphicsOverlays().add(polygonGraphicsOverlay);
      mapView.getGraphicsOverlays().add(polylineGraphicsOverlay);

      fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFF0000FF, null);
      polygonBuilder = new PolygonBuilder(WEB_MERCATOR);
      polygonBuilder.addPoint(new Point(-1.1981766578966897E7, 4811463.665479102, WEB_MERCATOR));
      polygonBuilder.addPoint(new Point(-1.1981766578966897E7, 4811279.118603914, WEB_MERCATOR));
      polygonBuilder.addPoint(new Point(-1.1981647913341776E7, 4811279.118603914, WEB_MERCATOR));
      polygonBuilder.addPoint(new Point(-1.1981647913341776E7, 4811463.665479102, WEB_MERCATOR));
      polygonGraphicsOverlay.getGraphics().add(new Graphic(polygonBuilder.toGeometry(), fillSymbol));

      SimpleLineSymbol outline = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF000000, 3.0f);
      polylineBuilder = new PolylineBuilder(WEB_MERCATOR);
      mapView.setOnMouseClicked(e -> {
        if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
          if (countPoints == 2) {
            resetView();
          }

          Point2D point = new Point2D(e.getX(), e.getY());
          Point mapPoint = mapView.screenToLocation(point);

          Point newPoint = new Point(mapPoint.getX(), mapPoint.getY(), WEB_MERCATOR);
          polylineBuilder.addPoint(newPoint);
          polylineGraphicsOverlay.getGraphics().add(new Graphic(polylineBuilder.toGeometry(), outline));
          countPoints++;

          if (countPoints == 2) {
            testGeometryCrossing();
          }

        } else if (e.getButton() == MouseButton.SECONDARY && e.isStillSincePress()) {
          resetView();
        }
      });

      stackPane.setCenter(mapView);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Test if polyline crosses, one point outside polygon and another inside, the polygon and changes the color of the 
   * polygon.
   * <p>
   * If polygon red, then polyline doesn't cross.
   * if polygon green, then polyline does cross.
   */
  public void testGeometryCrossing() {

    // 
    boolean isCrossing = GeometryEngine.crosses(polylineBuilder.toGeometry(), polygonBuilder.toGeometry());
    if (isCrossing) {
      fillSymbol.setColor(0xFF00FF00);
    } else {
      fillSymbol.setColor(0xFFFF0000);
    }
  }

  private void resetView() {
    polylineGraphicsOverlay.getGraphics().clear();
    polylineBuilder = new PolylineBuilder(WEB_MERCATOR);
    countPoints = 0;
    fillSymbol.setColor(0xFF0000FF);
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
