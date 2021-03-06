package DePaul.SE459.Simulator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import DePaul.SE459.CleanSweep.Floor;
import DePaul.SE459.CleanSweep.FloorPlan;
import DePaul.SE459.CleanSweep.Tile;

/**
 * This class contains all of the logic related to loading and dumping floor plans to and from files.
 */
public final class FloorPlanUtility {
	private FloorPlanUtility() {

	}

	/**
	 * Loads an XML file from disk and parses it into a floor plan.
	 * @param doc 
	 * @return The FloorPlan, complete with Floors and Tiles.
	 * @throws Exception If there is an IO error or file does not meet predefined floor plan layout.
	 */
	public static FloorPlan loadFloorPlan(Document doc) throws Exception {
		FloorPlan floorPlan = new FloorPlan();

		if (doc == null) {
			throw new Exception("FloorPlanUtility.loadFloorPlan: Document object cannot be null");
		}

		doc.getDocumentElement().normalize();

		// for each floor
		NodeList floors = doc.getElementsByTagName("floor");
		for (int i = 0; i < floors.getLength(); i++) {
			Node floor = floors.item(i);

			if (floor.getNodeType() == Node.ELEMENT_NODE) {
				Element floorElements = (Element) floor;
				int level = Integer.parseInt(floorElements.getAttribute("level"));
				Floor f = new Floor(level);
				floorPlan.addFloor(f);

				// for each cell
				NodeList cells = floor.getChildNodes();
				for (int ii = 0; ii < cells.getLength(); ii++) {
					Node cell = cells.item(ii);

					if (cell.getNodeType() == Node.ELEMENT_NODE) {
						Element cellElements = (Element) cell;

						int xPos = Integer.parseInt(cellElements.getAttribute("xs"));
						int yPos = Integer.parseInt(cellElements.getAttribute("ys"));
						int surface = Integer.parseInt(cellElements.getAttribute("ss"));

						String paths = cellElements.getAttribute("ps");

						int right = Integer.parseInt(paths.substring(0, 1));
						int left = Integer.parseInt(paths.substring(1, 2));
						int up = Integer.parseInt(paths.substring(2, 3));
						int down = Integer.parseInt(paths.substring(3, 4));

						int dirt = Integer.parseInt(cellElements.getAttribute("ds"));
						int charge = Integer.parseInt(cellElements.getAttribute("cs"));

						Boolean charger = false;
						if (charge == 1) {
							charger = true;
						}

						Tile t = new Tile(xPos, yPos, surface, right, left, up, down, dirt, charger);
						f.addTile(t);
					}
				}

				buildAdjacentTiles(f);
			}
		}

		return floorPlan;
	}

	public static void buildAdjacentTiles(Floor floor) {
		Iterator<Entry<Integer, Tile>> it = floor.getTiles().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Tile> pairs = it.next();
			Tile currentTile = pairs.getValue();

			if (currentTile.isChargingStation()) {
				floor.setHomeTile(currentTile);
			}

			if (currentTile.getRightPath() < 2) {
				Tile rightTile = floor.getTile(currentTile.getX() + 1, currentTile.getY());

				if (rightTile != null) {
					currentTile.setRightTile(rightTile);
				}
			}
			if (currentTile.getLeftPath() < 2) {
				Tile leftTile = floor.getTile(currentTile.getX() - 1, currentTile.getY());

				if (leftTile != null) {
					currentTile.setLeftTile(leftTile);
				}
			}
			if (currentTile.getLowerPath() < 2) {
				Tile lowerTile = floor.getTile(currentTile.getX(), currentTile.getY() - 1);

				if (lowerTile != null) {
					currentTile.setLowerTile(lowerTile);
				}
			}
			if (currentTile.getUpperPath() < 2) {
				Tile upperTile = floor.getTile(currentTile.getX(), currentTile.getY() + 1);

				if (upperTile != null) {
					currentTile.setUpperTile(upperTile);
				}
			}
		}
	}

	public static Document loadFromFile(String filePath) throws Exception {
		File file = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(file);

		return doc;
	}

	public static Document loadFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		return builder.parse(new ByteArrayInputStream(xml.getBytes()));
	}
}
