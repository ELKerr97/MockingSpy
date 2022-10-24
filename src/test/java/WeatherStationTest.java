import com.sun.source.tree.ModuleTree;
import instruments.Anemometer;
import instruments.Barometer;
import instruments.Hygrometer;
import instruments.Thermometer;
import instruments.satellite.SatelliteDataCache;
import instruments.satellite.SatelliteUplink;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class WeatherStationTest {

  WeatherStation weatherStation;
  SatelliteUplink satelliteUplink;
  Anemometer anemometerMock;
  Barometer barometerMock;
  Hygrometer hygrometerMock;
  Thermometer thermometerMock;

  /**
   * New instances before each test
   */
  @BeforeEach
  public void setUp() {
    anemometerMock = Mockito.mock(Anemometer.class);
    barometerMock = Mockito.mock(Barometer.class);
    hygrometerMock = Mockito.mock(Hygrometer.class);
    thermometerMock = Mockito.mock(Thermometer.class);
    satelliteUplink = Mockito.spy(new SatelliteUplink());
    weatherStation= new WeatherStation(anemometerMock, barometerMock,
            hygrometerMock, thermometerMock, satelliteUplink);
  }

  @Test
  public void satelliteTest(){
    Assertions.assertTrue(satelliteUplink.runStormCheckForArea(0.0,0.0,
            0.0));
    Assertions.assertTrue(satelliteUplink.runTornadoCheckForArea(0.0,0.0,
            0.0));
    SatelliteDataCache cacheSpy = Mockito.mock(SatelliteDataCache.class);
    Mockito.when(cacheSpy.getBarometricPressure()).thenReturn(900.0);
    Assertions.assertTrue(satelliteUplink.checkNearbyAreaTornadoes());

  }

  /**
   * Test a successful call of runStormWarningCheck()
   */
  @Test
  public void stormWarningTest() {
    weatherStation.setSatelliteUplink(satelliteUplink);
    weatherStation.setBarometer(barometerMock);
    weatherStation.setAnemometer(anemometerMock);
    weatherStation.setHygrometer(hygrometerMock);
    weatherStation.setThermometer(thermometerMock);
    satelliteUplink.runStormCheckForArea(60.0,0.0,80.0);
    weatherStation.runStormWarningCheck();

    Mockito.verify(satelliteUplink, Mockito.times(1)).checkNearbyAreaStorms();

    // Test for when air pressure is over 800
    Mockito.when(barometerMock.getAtmosphericPressure()).thenReturn(900.0);
    Assertions.assertFalse(weatherStation.runStormWarningCheck());

  }

  @Test
  public void tornadoWarningTest() {
    SatelliteDataCache cacheMock = Mockito.mock(SatelliteDataCache.class);
    Mockito.when(cacheMock.getBarometricPressure()).thenReturn(900.0);
    Assertions.assertTrue(satelliteUplink.runTornadoCheckForArea(0.0,900.0,
            0.0));
    Assertions.assertTrue(satelliteUplink.checkNearbyAreaTornadoes());

  }

  @Test
  public void runTornadoWarningCheckTest() {
    Mockito.when(hygrometerMock.getCurrentHumidity()).thenReturn(0.0,80.0);
    Mockito.when(anemometerMock.getWindSpeed()).thenReturn(0.0, 40.0);
    Mockito.when(barometerMock.getAtmosphericPressure()).thenReturn(900.0, 0.0);

    Assertions.assertFalse(weatherStation.runTornadoWarningCheck());
    Assertions.assertTrue(weatherStation.runTornadoWarningCheck());

    Mockito.verify(anemometerMock, Mockito.times(2)).getWindSpeed();
    Mockito.verify(barometerMock, Mockito.times(2)).getAtmosphericPressure();
    Mockito.verify(hygrometerMock, Mockito.times(2)).getCurrentHumidity();
  }

  @Test
  public void anemometerCalibrationCheckTest() {
    Mockito.when(anemometerMock.getWindSpeed()).thenReturn(1.0, 0.0);
    Mockito.when(anemometerMock.getWindDirInDegrees()).thenReturn(30.0, 40.0);

    Assertions.assertTrue(weatherStation.anemometerCalibrationCheck());

    Mockito.when(anemometerMock.getWindSpeed()).thenReturn(1.0, 5.0);
    Mockito.when(anemometerMock.getWindDirInDegrees()).thenReturn(10.0, 40.0);

    Assertions.assertFalse(weatherStation.anemometerCalibrationCheck());

  }

  @Test
  public void temperatureCalibrationTest_Test() {
    Mockito.when(thermometerMock.getCurrentTemperature()).thenReturn(67.6, 67.9);
    Assertions.assertTrue(weatherStation.temperatureCalibrationTest());

    Mockito.when(thermometerMock.getCurrentTemperature()).thenReturn(62.6, 67.9);
    Assertions.assertFalse(weatherStation.temperatureCalibrationTest());
  }

}
