
Geospatial data viewer
----------------------

Plugin page: [http://artifacts.griffon-framework.org/plugin/geotools](http://artifacts.griffon-framework.org/plugin/geotools)


[GeoTools][1] is an open source Java library that provides tools for geospatial data.

Usage
-----

The following nodes will become available on a View script upon installing this plugin

| *Node*    | *Type*                          |
| --------- | ------------------------------- |
| mapPane   | `org.geotools.swing.JMapPane`   |
| mapViewer | `org.geotools.swing.JMapViewer` |

Refer to the Javadocs found in the plugin's distribution to learn more about the properties that can be set on these nodes.

### Example

The following example is a reproduction of the basic [NetBeans app][2] available from Geotools' tutorials

__SampleView.groovy__

        package sample

        import org.geotools.renderer.lite.StreamingRenderer
        import org.geotools.swing.JMapViewer

        application(title: 'Geotools',
                preferredSize: [320, 240],
                pack: true,
                locationByPlatform: true,
                iconImage: imageIcon('/griffon-icon-48x48.png').image,
                iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                        imageIcon('/griffon-icon-32x32.png').image,
                        imageIcon('/griffon-icon-16x16.png').image]) {
            borderLayout()
            button('Load', constraints: NORTH, actionPerformed: controller.load)
            scrollPane(constraints: CENTER) {
                widget(new JMapViewer(), id: 'map', showStatusBar: false,
                        background: Color.WHITE, renderer: new StreamingRenderer())
            }
        }

__SampleController.groovy__

        package sample

        import griffon.transform.Threading
        import org.geotools.data.FileDataStore
        import org.geotools.data.FileDataStoreFinder
        import org.geotools.data.simple.SimpleFeatureSource
        import org.geotools.map.*
        import org.geotools.styling.SLD
        import org.geotools.swing.data.JFileDataStoreChooser

        class SampleController {
            def view

            @Threading(Threading.Policy.SKIP)
            def load = {
                File file = JFileDataStoreChooser.showOpenFile("shp", null);
                if (file == null) {
                    return;
                }

                FileDataStore store = FileDataStoreFinder.getDataStore(file);
                SimpleFeatureSource featureSource = store.getFeatureSource();

                MapContent mapContent = new MapContent();
                mapContent.setTitle("Quickstart");

                def style = SLD.createSimpleStyle(featureSource.getSchema());
                Layer layer = new FeatureLayer(featureSource, style);
                mapContent.addLayer(layer);

                view.map.mapContent = mapContent
            }
        }


[1]: http://geotools.org/
[2]: http://docs.geotools.org/latest/userguide/tutorial/quickstart/netbeans.html

