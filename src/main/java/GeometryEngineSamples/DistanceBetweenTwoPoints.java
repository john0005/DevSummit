
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

import java.text.DecimalFormat;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import com.esri.arcgisruntime.geometry.AngularUnit;
import com.esri.arcgisruntime.geometry.AngularUnitId;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol.HorizontalAlignment;
import com.esri.arcgisruntime.symbology.TextSymbol.VerticalAlignment;

public class DistanceBetweenTwoPoints extends Application {

  private MapView mapView;
  private GraphicsOverlay graphicsOverlay;
  private PolygonBuilder polygonBuilder;
  private PointCollection pointCollection;
  private SimpleFillSymbol fillSymbol;
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
      mapView.setViewpoint(new Viewpoint(33.747252, -112.633853, 20000));

      graphicsOverlay = new GraphicsOverlay();
      mapView.getGraphicsOverlays().add(graphicsOverlay);

      fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFFFFFFFF, null);
      polygonBuilder = new PolygonBuilder(WEB_MERCATOR);
      polygonBuilder.addPoint(new Point(-1.253914220371191E7, 3996271.738049853, WEB_MERCATOR));
      polygonBuilder.addPoint(new Point(-1.2537522953710258E7, 3996271.738049853, WEB_MERCATOR));
      polygonBuilder.addPoint(new Point(-1.2537522953710258E7, 3995628.8005491975, WEB_MERCATOR));
      polygonBuilder.addPoint(new Point(-1.253914220371191E7, 3995628.8005491975, WEB_MERCATOR));

      SimpleMarkerSymbol circleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 20);
      pointCollection = new PointCollection(WEB_MERCATOR);

      mapView.setOnMouseClicked(e -> {
        if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
          Point2D point = new Point2D(e.getX(), e.getY());
          Point mapPoint = mapView.screenToLocation(point);

          Point newPoint = new Point(mapPoint.getX(), mapPoint.getY(), WEB_MERCATOR);
          pointCollection.add(newPoint);
          Graphic graphic = new Graphic(newPoint, circleSymbol);
          graphicsOverlay.getGraphics().add(graphic);

          if (pointCollection.size() == 2) {
            showDistanceBetweenPoints();
          }
        } else if (e.getButton() == MouseButton.SECONDARY && e.isStillSincePress()) {
          graphicsOverlay.getGraphics().clear();
          pointCollection.clear();
        }
      });

      stackPane.setCenter(mapView);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the distance between two points and display it to the MapView.
   */
  public void showDistanceBetweenPoints() {

    // get distance between two points
    GeodeticDistanceResult result =
        GeometryEngine.distanceGeodetic(pointCollection.get(0), pointCollection.get(1),
            new LinearUnit(LinearUnitId.METERS), new AngularUnit(AngularUnitId.DEGREES),
            GeodeticCurveType.GEODESIC);

    // Display distance to user
    graphicsOverlay.getGraphics().add(new Graphic(polygonBuilder.toGeometry(), fillSymbol));
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(2);
    TextSymbol text =
        new TextSymbol(
            35,
            "" + df.format(result.getDistance()) + " " + result.getDistanceUnit().getDisplayName() + "s",
            0xFF000000,
            HorizontalAlignment.CENTER,
            VerticalAlignment.MIDDLE);
    text.setFontWeight(TextSymbol.FontWeight.BOLD);
    graphicsOverlay.getGraphics().add(new Graphic(GeometryEngine.labelPoint(polygonBuilder.toGeometry()), text));
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
