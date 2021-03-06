package com.itproject.game;

import com.itproject.game.buildings.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class City {
	public interface CityListener {
		// To do
	}
	
	public static final float CITY_WIDTH = 10; // to change
	public static final float CITY_HEIGHT = 15 * 20; // to change
	
	public static final int CITY_STATE_RUNNING = 0;
	public static final int CITY_STATE_GAME_OVER = 1;

	float gameTime;
	public static Time time;

	static final Random DEFAULT_PRNG = new Random();
	public static Random PRNG;
	public static BiasedRandom BPRNG;

	static float[] birthStatistics = new float[2];
	static float[] procreateStatistics = new float[2];
	static float[] deathStatistics = new float[117];
	static float[] expensesStatistics = new float[4];

	public static float[] jobDistribution = new float[10];

	CityListener listener;
	public static List<Citizen> citizens;
	public static List<Building> buildings;
	List<Road> road;

	public static Worldview worldview;
	public static int deathCounter;
	public static int birthCounter;
	public static Budget budget;
	int state;
	public static float averageHappynessLevel;

	public City(CityListener listener) {
		loadStatistics();

		this.citizens = new ArrayList<Citizen>();
		this.buildings = new ArrayList<Building>();
		this.road = new ArrayList<Road>();
		this.listener = listener;
		this.PRNG = DEFAULT_PRNG;
		this.BPRNG = new BiasedRandom(this);
		this.worldview = new Worldview(this);
		this.budget = new Budget(this);
		this.time = new Time();
		budget.makeProgressive();
		//lul
		this.time.nextDay();

		deathCounter = birthCounter = 0;
		// Create map generation later
		// generateMap();

        // Init. population
		citizens.add(new Citizen(Worldview.WorldviewType.WORLDVIEW1, new Interval((byte) 0, (byte)0, (byte)18), (short) 3600));
		citizens.add(new Citizen(Worldview.WorldviewType.WORLDVIEW2, new Interval((byte) 0, (byte)0, (byte)18), (short) 3600));
		citizens.add(new Citizen(Worldview.WorldviewType.WORLDVIEW3, new Interval((byte) 0, (byte)0, (byte)18), (short) 3600));

		for (int i = 0; i < 297; i++) {
			citizens.add(new Citizen(worldview.determineType(), new Interval((byte) 0, (byte)0, (byte)18), (short) 3600));
		}
	}
	
	/*private void generateMap() {
		
	}*/
	
	public void update(float deltaTime) {
		gameTime += deltaTime;
		if (gameTime >= 1f) {
			updateBuildings();

            updateCitizens();
			updatePopulation();

            findHouse();
            findJob();

            if (time.getDay() == 1) {
            	deathCounter = birthCounter = 0;
            }
            
			updateTime();
			System.out.println("Citizen population: " + citizens.size());

            gameTime -= 1f;
		}

		buildings.forEach(Building::updateSelected);
        checkGameOver();
	}

	public void loadStatistics() {
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse("../core/assets/statistics.xml");

			Node root = document.getDocumentElement();
			NodeList childNodes = root.getChildNodes();

			Node node;

			float[][] statistics = new float[4][];
			statistics[0] = birthStatistics;
			statistics[1] = procreateStatistics;
			statistics[2] = deathStatistics;
			statistics[3] = expensesStatistics;

			for (int i = 0; i < childNodes.getLength(); i++) {
				node = childNodes.item(i);
				if (node.getTextContent().trim().equals("")) {
					node.getParentNode().removeChild(node);
					i--;
				} else {
					NodeList statisticsData = node.getChildNodes();
					for (int j = 0; j < statisticsData.getLength(); j++) {
						node = statisticsData.item(j);
						if (node.getTextContent().trim().equals("")) {
							node.getParentNode().removeChild(node);
							j--;
						} else {
							statistics[i][j] = Float.parseFloat(node.getTextContent());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private void updatePopulation() {
		List<Citizen> newCitizens = new ArrayList<>();

        Citizen firstParent = null;
		Citizen secondParent = null;

		int throwDie;
		short moneyForChild = 0;
		float happinessContribution = 0;

		Iterator<Citizen> citizen = citizens.iterator();
		while (citizen.hasNext()) {
			if (citizen.hasNext()) {
				firstParent = citizen.next();
			}

			if (citizen.hasNext()) {
				secondParent = citizen.next();
			}

			if ( (firstParent != null && secondParent != null) &&
					(firstParent.isReadyToProcreate && secondParent.isReadyToProcreate)) {
                if (firstParent.happinessLevel > 60 && secondParent.happinessLevel > 60) {
					happinessContribution = ((firstParent.happinessLevel - 60) + (secondParent.happinessLevel - 60)) / 10000;
					procreateStatistics[1] += happinessContribution;
				} else if (firstParent.happinessLevel < 45 && secondParent.happinessLevel < 45) {
					happinessContribution = ((45 - firstParent.happinessLevel) + (45 - secondParent.happinessLevel)) / -10000;
					procreateStatistics[1] += happinessContribution;
				}

				throwDie = BPRNG.nextByte(procreateStatistics, (short)1000);
                if (throwDie == 1) {
					throwDie = BPRNG.nextByte(birthStatistics, (short)100);
					if (throwDie == 0) {
						if (firstParent.moneySavings - 1200 >= 0) {
							firstParent.moneySavings -= 1200;
							moneyForChild += 1200;
						} else {
							moneyForChild += (short) firstParent.moneySavings;
							firstParent.moneySavings = 0;
						}

						if (secondParent.moneySavings - 1200 >= 0) {
							secondParent.moneySavings -= 1200;
							moneyForChild += 1200;
						} else {
							moneyForChild += (short) secondParent.moneySavings;
							secondParent.moneySavings = 0;
						}

						birthCounter++;
						newCitizens.add(new Citizen(worldview.determineType(), moneyForChild));
					} else {
						if (firstParent.moneySavings - 2400 >= 0) {
							firstParent.moneySavings -= 2400;
							moneyForChild += 1200;
						} else {
							moneyForChild += (short) (firstParent.moneySavings >> 1);
							firstParent.moneySavings = 0;
						}

						if (secondParent.moneySavings - 2400 >= 0) {
							secondParent.moneySavings -= 2400;
							moneyForChild += 1200;
						} else {
							moneyForChild += (short) (secondParent.moneySavings >> 1);
							secondParent.moneySavings = 0;
						}
						
						birthCounter++;
						birthCounter++;
						newCitizens.add(new Citizen(worldview.determineType(), moneyForChild));
						newCitizens.add(new Citizen(worldview.determineType(), moneyForChild));
					}

					firstParent.ageOfLastProcreation.concatenateWith(firstParent.age.subtractInterval(firstParent.ageOfLastProcreation));
                    secondParent.ageOfLastProcreation.concatenateWith(secondParent.age.subtractInterval(secondParent.ageOfLastProcreation));
					secondParent.isReadyToProcreate = firstParent.isReadyToProcreate = false;
					moneyForChild = 0;
				}

				if (happinessContribution != 0) {
					procreateStatistics[1] = 0.006f;
					happinessContribution = 0;
				}
			}

            firstParent = secondParent = null;
		}

		citizens.addAll(newCitizens);
    }

	private void updateCitizens() {
		for (int i = 0; i < jobDistribution.length; i++) {
			jobDistribution[i] = 0;
		}
		averageHappynessLevel = 0;

        List<Citizen> deadCitizens = new ArrayList<>();

		int previousYear;
        for (Citizen c : citizens) {
			previousYear = c.age.getYear();
			c.update();

			averageHappynessLevel += c.happinessLevel;

			switch (c.occupation) {
				case IRONPLANTWORKER:
					jobDistribution[0]++;
					break;
				case OILPLANTWORKER:
					jobDistribution[1]++;
					break;
				case TRADER:
					jobDistribution[2]++;
					break;
				case SELLER:
					jobDistribution[3]++;
					break;
				case BANKER:
					jobDistribution[4]++;
					break;
				case CLERK:
					jobDistribution[5]++;
					break;
				case DOCTOR:
					jobDistribution[6]++;
					break;
				case WATERSTATIONWORKER:
					jobDistribution[7]++;
					break;
				case POWERSTATIONWORKER:
					jobDistribution[8]++;
					break;
				case UNEMPLOYED:
					jobDistribution[9]++;
					break;
			}

			if (c.age.getYear() > previousYear || (c.age.getYear() == 0 && c.age.getMonth() == 0 && c.age.getDay() == 7) || c.biasedDeathProbability >= 1) {
                if(!c.isAlive()) {
                	deathCounter++;
					deadCitizens.add(c);
                }
            }
		}

		if (!deadCitizens.isEmpty()) {
			for (Building building : buildings) {
				if (building instanceof PowerStation) {
					((PowerStation) building).employees.removeAll(deadCitizens);
				} else if (building instanceof WaterStation) {
					((WaterStation) building).employees.removeAll(deadCitizens);
				} else if (building instanceof Hospital) {
					((Hospital) building).doctors.removeAll(deadCitizens);
				} else if (building instanceof Bank) {
					((Bank) building).clerks.removeAll(deadCitizens);
					((Bank) building).bankers.removeAll(deadCitizens);
				} else if (building instanceof WorldTradeCenter) {
					((WorldTradeCenter) building).salespeople.removeAll(deadCitizens);
					((WorldTradeCenter) building).traders.removeAll(deadCitizens);
				} else if (building instanceof GroceryShop) {
					((GroceryShop) building).salespeople.removeAll(deadCitizens);
				} else if (building instanceof IronPlant) {
					((IronPlant) building).workers.removeAll(deadCitizens);
				} else if (building instanceof OilPlant) {
					((OilPlant) building).workers.removeAll(deadCitizens);
				} else if (building instanceof House) {
					((House) building).residents.removeAll(deadCitizens);
				}
			}
			citizens.removeAll(deadCitizens);
		}

		for (int i = 0; i < jobDistribution.length; i++) {
			jobDistribution[i] = jobDistribution[i] / citizens.size();
		}
		averageHappynessLevel = averageHappynessLevel / citizens.size();

		System.out.println("IRONPLANTWORKER: " + jobDistribution[0]);
		System.out.println("OILPLANTWORKER: " + jobDistribution[1]);
		System.out.println("TRADER: " + jobDistribution[2]);
		System.out.println("SELLER: " + jobDistribution[3]);
		System.out.println("BANKER: " + jobDistribution[4]);
		System.out.println("CLERK: " + jobDistribution[5]);
		System.out.println("DOCTOR: " + jobDistribution[6]);
		System.out.println("WATERSTATIONWORKER: " + jobDistribution[7]);
		System.out.println("POWERSTATIONWORKER: " + jobDistribution[8]);
		System.out.println("UNEMPLOYED: " + jobDistribution[9]);
		System.out.println("");
		System.out.println("Average happiness level: " + averageHappynessLevel);
		System.out.println("");
	}

	public void findJob() {
        Iterator<Building> iterator = buildings.iterator();
        Building building = (iterator.hasNext()) ? iterator.next() : null;

        if (building != null) {
            for (Citizen citizen : citizens) {
                if (citizen.occupation == Citizen.Occupation.UNEMPLOYED) {
                    do {
                        if (building instanceof PowerStation) {
                            if (((PowerStation) building).hireEmployee(citizen)) {
                                break;
                            }
                        } else if (building instanceof WaterStation) {
                            if (((WaterStation) building).hireEmployee(citizen)) {
                                break;
                            }
                        } else if (building instanceof WorldTradeCenter) {
							if (((WorldTradeCenter) building).hireEmployee(citizen)) {
								break;
							}
						} else if (building instanceof GroceryShop) {
							if (((GroceryShop) building).hireEmployee(citizen)) {
								break;
							}
						} else if (building instanceof Bar) {
							if (((Bar) building).hireEmployee(citizen)) {
								break;
							}
						} else if (building instanceof IronPlant) {
							if (((IronPlant) building).hireEmployee(citizen)) {
								break;
							}
						} else if (building instanceof OilPlant) {
							if (((OilPlant) building).hireEmployee(citizen)) {
								break;
							}
						} else if (building instanceof Hospital) {
                            if (((Hospital) building).hireEmployee(citizen)) {
                                break;
                            }
                        } else if (building instanceof Bank) {
                            if (((Bank) building).hireEmployee(citizen)) {
                                break;
                            }
                        }

                        if (!iterator.hasNext()) {
                            break;
                        }
                        building = iterator.next();

                    } while (true);

                    if (!iterator.hasNext()) {
                        iterator = buildings.iterator();
                    }
                    building = iterator.next();
                }
            }
        }
    }

	public void findHouse() 	{
        boolean isSettled;

        Iterator<Building> iterator = buildings.iterator();
        Building building = (iterator.hasNext()) ? iterator.next() : null;

        for (Citizen citizen : citizens) {
            if (citizen.house == null) {
                do {
                    if (building == null) {
                        break;
                    }

                    if (!(building instanceof House)) {
                        while (iterator.hasNext() && !((building = iterator.next()) instanceof House)) {}
                    }

                    if (!iterator.hasNext() && !(building instanceof House)) {
                        break;
                    }

                    if (citizen.moneySavings < ((House) building).utilityBill) {
                        break;
                    }

                    isSettled = ((House) building).settleResident(citizen);
                    if (!isSettled) {
                        building = (iterator.hasNext()) ? iterator.next() : null;
                    } else {
                        break;
                    }
                } while (true);
            }
        }
    }

	private void updateBuildings() {
		buildings.forEach(building -> {
			if (building instanceof PowerStation || building instanceof WaterStation) {
				building.update();
			}
		});

		buildings.forEach(building -> {
			if (!(building instanceof PowerStation) && !(building instanceof WaterStation)) {
				building.update();
			}
		});
	}

	public void updateTime() {
		time.nextDay();
	//	System.out.println(City.time.toString());
	}

	private void checkGameOver() {

	}
}
