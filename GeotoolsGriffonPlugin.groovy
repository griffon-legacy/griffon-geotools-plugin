/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
class GeotoolsGriffonPlugin {
    // the plugin version
    String version = '0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '0.9.5 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [swing: '0.9.5']
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, qt
    List toolkits = ['swing']
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-geotools-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Geospatial data viewer'
    // accepts Markdown syntax. See http://daringfireball.net/projects/markdown/ for details
    String description = '''
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
'''
}
