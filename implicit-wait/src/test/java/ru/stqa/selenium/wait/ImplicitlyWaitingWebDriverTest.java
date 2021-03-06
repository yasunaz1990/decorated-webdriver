/*
 * Copyright 2013 Alexei Barantsev
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package ru.stqa.selenium.wait;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ImplicitlyWaitingWebDriverTest {

  private TestingClock clock;
  private WebDriver mockedDriver;
  private WebDriver driver;

  @BeforeEach
  void setUp() {
    clock = new TestingClock();
    mockedDriver = getMockedDriver();
    driver = new ImplicitlyWaitingWebDriver(mockedDriver, clock, clock, 1, 100).getActivated();
  }

  private WebDriver getMockedDriver() {
    final WebDriver mockedDriver = mock(WebDriver.class,
        withSettings().extraInterfaces(HasInputDevices.class, Interactive.class));
    final WebDriver.Options mockedOptions = mock(WebDriver.Options.class);
    final WebDriver.Timeouts mockedTimeouts = mock(WebDriver.Timeouts.class);
    when(mockedDriver.manage()).thenReturn(mockedOptions);
    when(mockedOptions.timeouts()).thenReturn(mockedTimeouts);
    when(mockedTimeouts.implicitlyWait(0, TimeUnit.SECONDS)).thenReturn(null);
    return mockedDriver;
  }

  @Test
  void findElementShouldImplicitlyWaitForAnElementToBePresent() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement);

    WebElement element = driver.findElement(By.name("foo"));

    assertThat(clock.now(), is(200L));
    verify(mockedDriver, times(3)).findElement(By.name("foo"));
    assertThat(element, equalTo(mockedElement));
  }

  @Test
  void findElementShouldThrowIfElementIsNotFound() {
    when(mockedDriver.findElement(By.name("foo")))
        .thenThrow(NoSuchElementException.class);

    assertThrows(NoSuchElementException.class, () -> driver.findElement(By.name("foo")));

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(11)).findElement(By.name("foo"));
  }

  @Test
  void findElementsShouldImplicitlyWaitForAtLeastOneElementToBePresent() {
    final WebElement mockedElement1 = mock(WebElement.class);
    final WebElement mockedElement2 = mock(WebElement.class);
    final List<WebElement> mockedElements = Lists.newArrayList(mockedElement1, mockedElement2);

    when(mockedDriver.findElements(By.name("foo")))
        .thenReturn(new ArrayList<>())
        .thenReturn(new ArrayList<>())
        .thenReturn(mockedElements);

    List<WebElement> elements = driver.findElements(By.name("foo"));

    assertThat(clock.now(), is(200L));
    verify(mockedDriver, times(3)).findElements(By.name("foo"));
    assertThat(elements, equalTo(mockedElements));
  }

  @Test
  void findElementsShouldReturnEmptyListIfNoElementIsFound() {
    when(mockedDriver.findElements(By.name("foo")))
        .thenReturn(new ArrayList<>());

    List<WebElement> elements = driver.findElements(By.name("foo"));

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(11)).findElements(By.name("foo"));
    assertThat(elements.size(), is(0));
  }

  @Test
  void clickShouldImplicitlyWaitForTheElementToBeVisible() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

   doThrow(ElementNotVisibleException.class)
        .doThrow(ElementNotVisibleException.class)
        .doNothing()
        .when(mockedElement).click();

    driver.findElement(By.name("foo")).click();

    assertThat(clock.now(), is(200L));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(3)).click();
  }

  @Test
  void clickShouldThrowIfTheElementIsNotVisible() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    doThrow(ElementNotVisibleException.class)
        .when(mockedElement).click();

    assertThrows(ElementNotVisibleException.class, () -> driver.findElement(By.name("foo")).click());

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(11)).click();
  }

  @Test
  void submitShouldImplicitlyWaitForTheElementToBeVisible() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    doThrow(ElementNotVisibleException.class)
        .doThrow(ElementNotVisibleException.class)
        .doNothing()
        .when(mockedElement).submit();

    driver.findElement(By.name("foo")).submit();

    assertThat(clock.now(), is(200L));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(3)).submit();
  }

  @Test
  void submitShouldThrowIfTheElementIsNotVisible() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    doThrow(ElementNotVisibleException.class)
        .when(mockedElement).submit();

    assertThrows(ElementNotVisibleException.class, () -> driver.findElement(By.name("foo")).submit());

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(11)).submit();
  }

  @Test
  void sendKeysShouldImplicitlyWaitForTheElementToBeVisible() {
    final String text = "To be or not to be";
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    doThrow(ElementNotVisibleException.class)
        .doThrow(ElementNotVisibleException.class)
        .doNothing()
        .when(mockedElement).sendKeys(text);

    driver.findElement(By.name("foo")).sendKeys(text);

    assertThat(clock.now(), is(200L));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(3)).sendKeys(text);
  }

  @Test
  void sendKeysShouldThrowIfTheElementIsNotVisible() {
    final String text = "To be or not to be";
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    doThrow(ElementNotVisibleException.class)
        .when(mockedElement).sendKeys(text);

    assertThrows(ElementNotVisibleException.class, () -> driver.findElement(By.name("foo")).sendKeys(text));

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(11)).sendKeys(text);
  }

  @Test
  void clearShouldImplicitlyWaitForTheElementToBeVisible() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    doThrow(ElementNotVisibleException.class)
        .doThrow(ElementNotVisibleException.class)
        .doNothing()
        .when(mockedElement).clear();

    driver.findElement(By.name("foo")).clear();

    assertThat(clock.now(), is(200L));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(3)).clear();
  }

  @Test
  void clearShouldThrowIfTheElementIsNotVisible() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    doThrow(new ElementNotVisibleException(""))
        .when(mockedElement).clear();

    assertThrows(ElementNotVisibleException.class, () -> driver.findElement(By.name("foo")).clear());

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(11)).clear();
  }

  @Test
  void isSelectedShouldImplicitlyWaitForTheElementToBeVisible() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    when(mockedElement.isSelected())
        .thenThrow(ElementNotVisibleException.class)
        .thenThrow(ElementNotVisibleException.class)
        .thenReturn(true);

    boolean selected = driver.findElement(By.name("foo")).isSelected();

    assertThat(clock.now(), is(200L));
    assertThat(selected, is(true));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(3)).isSelected();
  }

  @Test
  void isSelectedShouldImplicitlyWaitForTheElementToBeVisibleEvenIfTheElementIsNotSelected() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    when(mockedElement.isSelected())
        .thenThrow(new ElementNotVisibleException(""))
        .thenThrow(new ElementNotVisibleException(""))
        .thenReturn(false);

    boolean selected = driver.findElement(By.name("foo")).isSelected();

    assertThat(clock.now(), is(200L));
    assertThat(selected, is(false));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(3)).isSelected();
  }

  @Test
  void isSelectedShouldThrowIfTheElementIsNotVisible() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    when(mockedElement.isSelected())
        .thenThrow(ElementNotVisibleException.class);

    assertThrows(ElementNotVisibleException.class, () -> driver.findElement(By.name("foo")).isSelected());

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(11)).isSelected();
  }

  @Test
  void isEnabledShouldImplicitlyWaitForTheElementToBeVisible() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    when(mockedElement.isEnabled())
        .thenThrow(ElementNotVisibleException.class)
        .thenThrow(ElementNotVisibleException.class)
        .thenReturn(true);

    boolean selected = driver.findElement(By.name("foo")).isEnabled();

    assertThat(clock.now(), is(200L));
    assertThat(selected, is(true));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(3)).isEnabled();
  }

  @Test
  void isEnabledShouldImplicitlyWaitForTheElementToBeVisibleEvenIfTheElementIsNotEnabled() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    when(mockedElement.isEnabled())
        .thenThrow(new ElementNotVisibleException(""))
        .thenThrow(new ElementNotVisibleException(""))
        .thenReturn(false);

    boolean selected = driver.findElement(By.name("foo")).isEnabled();

    assertThat(clock.now(), is(200L));
    assertThat(selected, is(false));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(3)).isEnabled();
  }

  @Test
  void isEnabledShouldThrowIfTheElementIsNotVisible() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenReturn(mockedElement);

    when(mockedElement.isEnabled())
        .thenThrow(ElementNotVisibleException.class);

    assertThrows(ElementNotVisibleException.class, () -> driver.findElement(By.name("foo")).isEnabled());

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(1)).findElement(By.name("foo"));
    verify(mockedElement, times(11)).isEnabled();
  }

  @Test
  void findElementInAnotherElementShouldImplicitlyWaitForAnElementToBePresent() {
    final WebElement mockedElement = mock(WebElement.class);
    final WebElement mockedElement2 = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement);

    when(mockedElement.findElement(By.name("bar")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement2);

    WebElement element = driver.findElement(By.name("foo"));
    WebElement element2 = element.findElement(By.name("bar"));

    assertThat(clock.now(), is(400L));
    verify(mockedDriver, times(3)).findElement(By.name("foo"));
    verify(mockedElement, times(3)).findElement(By.name("bar"));
    assertThat(element2, equalTo(mockedElement2));
  }

  @Test
  void findElementInAnotherElementShouldThrowIfElementIsNotFound() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement);

    when(mockedElement.findElement(By.name("bar")))
        .thenThrow(NoSuchElementException.class);

    WebElement element = driver.findElement(By.name("foo"));

    assertThrows(NoSuchElementException.class, () -> element.findElement(By.name("bar")));

    assertThat(clock.now(), is(1200L));
    verify(mockedDriver, times(3)).findElement(By.name("foo"));
    verify(mockedElement, times(11)).findElement(By.name("bar"));
  }

  @Test
  void findElementsInAnotherElementShouldImplicitlyWaitForAtLeastOneElementToBePresent() {
    final WebElement mockedElement = mock(WebElement.class);
    final WebElement mockedElement1 = mock(WebElement.class);
    final WebElement mockedElement2 = mock(WebElement.class);
    final List<WebElement> mockedElements = Lists.newArrayList(mockedElement1, mockedElement2);

    when(mockedDriver.findElement(By.name("foo")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement);

    when(mockedElement.findElements(By.name("bar")))
        .thenReturn(new ArrayList<>())
        .thenReturn(new ArrayList<>())
        .thenReturn(mockedElements);

    WebElement element = driver.findElement(By.name("foo"));
    List<WebElement> elements = element.findElements(By.name("bar"));

    assertThat(clock.now(), is(400L));
    verify(mockedDriver, times(3)).findElement(By.name("foo"));
    verify(mockedElement, times(3)).findElements(By.name("bar"));
    assertThat(elements, equalTo(mockedElements));
  }

  @Test
  void findElementsInAnotherElementShouldReturnEmptyListIfNoElementIsFound() {
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement);

    when(mockedElement.findElements(By.name("foo")))
        .thenReturn(new ArrayList<>());

    WebElement element = driver.findElement(By.name("foo"));
    List<WebElement> elements = element.findElements(By.name("foo"));

    assertThat(clock.now(), is(1200L));
    verify(mockedDriver, times(3)).findElement(By.name("foo"));
    verify(mockedElement, times(11)).findElements(By.name("foo"));
    assertThat(elements.size(), is(0));
  }

  @Test
  void findElementChainShouldImplicitlyWaitForAnElementToBePresent() {
    final WebElement mockedElement1 = mock(WebElement.class);
    final WebElement mockedElement2 = mock(WebElement.class);
    final WebElement mockedElement3 = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement1);

    when(mockedElement1.findElement(By.name("bar")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement2);

    when(mockedElement2.findElement(By.name("baz")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement3);

    WebElement element1 = driver.findElement(By.name("foo"));
    WebElement element2 = element1.findElement(By.name("bar"));
    WebElement element3 = element2.findElement(By.name("baz"));

    assertThat(clock.now(), is(600L));
    verify(mockedDriver, times(3)).findElement(By.name("foo"));
    verify(mockedElement1, times(3)).findElement(By.name("bar"));
    verify(mockedElement2, times(3)).findElement(By.name("baz"));
    assertThat(element3, equalTo(mockedElement3));
  }

  @Test
  void findElementChainShouldThrowIfElementIsNotFound() {
    final WebElement mockedElement1 = mock(WebElement.class);
    final WebElement mockedElement2 = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement1);

    when(mockedElement1.findElement(By.name("bar")))
        .thenThrow(NoSuchElementException.class)
        .thenThrow(NoSuchElementException.class)
        .thenReturn(mockedElement2);

    when(mockedElement2.findElement(By.name("baz")))
        .thenThrow(NoSuchElementException.class);

    WebElement element1 = driver.findElement(By.name("foo"));
    WebElement element2 = element1.findElement(By.name("bar"));

    assertThrows(NoSuchElementException.class, () -> element2.findElement(By.name("baz")));

    assertThat(clock.now(), is(1400L));
    verify(mockedDriver, times(3)).findElement(By.name("foo"));
    verify(mockedElement1, times(3)).findElement(By.name("bar"));
    verify(mockedElement2, times(11)).findElement(By.name("baz"));
  }

  @Test
  void findElementsChainShouldImplicitlyWaitForAnElementToBePresent() {
    final WebElement mockedElement1 = mock(WebElement.class);
    final WebElement mockedElement2 = mock(WebElement.class);
    final WebElement mockedElement3 = mock(WebElement.class);

    when(mockedDriver.findElements(By.name("foo")))
        .thenReturn(new ArrayList<>())
        .thenReturn(new ArrayList<>())
        .thenReturn(Lists.newArrayList(mockedElement1));

    when(mockedElement1.findElements(By.name("bar")))
        .thenReturn(new ArrayList<>())
        .thenReturn(new ArrayList<>())
        .thenReturn(Lists.newArrayList(mockedElement2));

    when(mockedElement2.findElements(By.name("baz")))
        .thenReturn(new ArrayList<>())
        .thenReturn(new ArrayList<>())
        .thenReturn(Lists.newArrayList(mockedElement3));

    List<WebElement> elements1 = driver.findElements(By.name("foo"));
    List<WebElement> elements2 = elements1.get(0).findElements(By.name("bar"));
    List<WebElement> elements3 = elements2.get(0).findElements(By.name("baz"));

    assertThat(clock.now(), is(600L));
    verify(mockedDriver, times(3)).findElements(By.name("foo"));
    verify(mockedElement1, times(3)).findElements(By.name("bar"));
    verify(mockedElement2, times(3)).findElements(By.name("baz"));
    assertThat(elements3.get(0), equalTo(mockedElement3));
  }

  @Test
  void findElementsChainShouldThrowIfElementIsNotFound() {
    final WebElement mockedElement1 = mock(WebElement.class);
    final WebElement mockedElement2 = mock(WebElement.class);

    when(mockedDriver.findElements(By.name("foo")))
        .thenReturn(new ArrayList<>())
        .thenReturn(new ArrayList<>())
        .thenReturn(Lists.newArrayList(mockedElement1));

    when(mockedElement1.findElements(By.name("bar")))
        .thenReturn(new ArrayList<>())
        .thenReturn(new ArrayList<>())
        .thenReturn(Lists.newArrayList(mockedElement2));

    when(mockedElement2.findElements(By.name("baz")))
        .thenReturn(new ArrayList<>());

    List<WebElement> elements1 = driver.findElements(By.name("foo"));
    List<WebElement> elements2 = elements1.get(0).findElements(By.name("bar"));
    List<WebElement> elements3 = elements2.get(0).findElements(By.name("baz"));

    assertThat(clock.now(), is(1400L));
    verify(mockedDriver, times(3)).findElements(By.name("foo"));
    verify(mockedElement1, times(3)).findElements(By.name("bar"));
    verify(mockedElement2, times(11)).findElements(By.name("baz"));
    assertThat(elements3.size(), is(0));
  }

  @Test
  void switchToAlertShouldImplicitlyWaitForAnAlertToBePresent() {
    final WebDriver.TargetLocator mockedSwitch = mock(WebDriver.TargetLocator.class);
    final Alert mockedAlert = mock(Alert.class);

    when(mockedDriver.switchTo()).thenReturn(mockedSwitch);
    when(mockedSwitch.alert())
        .thenThrow(NoAlertPresentException.class)
        .thenThrow(NoAlertPresentException.class)
        .thenReturn(mockedAlert);

    Alert alert = driver.switchTo().alert();

    assertThat(clock.now(), is(200L));
    verify(mockedDriver, times(1)).switchTo();
    verify(mockedSwitch, times(3)).alert();
    assertThat(alert, is(mockedAlert));
  }

  @Test
  void switchToAlertShouldThrowIfThereIsNoAlert() {
    final WebDriver.TargetLocator mockedSwitch = mock(WebDriver.TargetLocator.class);

    when(mockedDriver.switchTo()).thenReturn(mockedSwitch);
    when(mockedSwitch.alert())
        .thenThrow(NoAlertPresentException.class);

    assertThrows(NoAlertPresentException.class, () -> driver.switchTo().alert());

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(1)).switchTo();
    verify(mockedSwitch, times(11)).alert();
  }

  @Test
  void switchToFrameByIndexShouldImplicitlyWaitForAFrameToBePresent() {
    final WebDriver.TargetLocator mockedSwitch = mock(WebDriver.TargetLocator.class);

    when(mockedDriver.switchTo()).thenReturn(mockedSwitch);
    when(mockedSwitch.frame(1))
        .thenThrow(NoSuchFrameException.class)
        .thenThrow(NoSuchFrameException.class)
        .thenReturn(mockedDriver);

    WebDriver newDriver = driver.switchTo().frame(1);

    assertThat(clock.now(), is(200L));
    verify(mockedDriver, times(1)).switchTo();
    verify(mockedSwitch, times(3)).frame(1);
    //assertThat(driver, sameInstance(newDriver));
  }

  @Test
  void switchToFrameByIndexShouldThrowIfThereIsNoFrame() {
    final WebDriver.TargetLocator mockedSwitch = mock(WebDriver.TargetLocator.class);

    when(mockedDriver.switchTo()).thenReturn(mockedSwitch);
    when(mockedSwitch.frame(1))
        .thenThrow(NoSuchFrameException.class);

    assertThrows(NoSuchFrameException.class, () -> driver.switchTo().frame(1));

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(1)).switchTo();
    verify(mockedSwitch, times(11)).frame(1);
  }

  @Test
  void switchToFrameByNameShouldImplicitlyWaitForAFrameToBePresent() {
    final WebDriver.TargetLocator mockedSwitch = mock(WebDriver.TargetLocator.class);

    when(mockedDriver.switchTo()).thenReturn(mockedSwitch);
    when(mockedSwitch.frame("myname"))
        .thenThrow(NoSuchFrameException.class)
        .thenThrow(NoSuchFrameException.class)
        .thenReturn(mockedDriver);

    WebDriver newDriver = driver.switchTo().frame("myname");

    assertThat(clock.now(), is(200L));
    verify(mockedDriver, times(1)).switchTo();
    verify(mockedSwitch, times(3)).frame("myname");
    //assertThat(driver, sameInstance(newDriver));
  }

  @Test
  void switchToFrameByNameShouldThrowIfThereIsNoFrame() {
    final WebDriver.TargetLocator mockedSwitch = mock(WebDriver.TargetLocator.class);

    when(mockedDriver.switchTo()).thenReturn(mockedSwitch);
    when(mockedSwitch.frame("myname"))
        .thenThrow(NoSuchFrameException.class);

    assertThrows(NoSuchFrameException.class, () -> driver.switchTo().frame("myname"));

    assertThat(clock.now(), is(1000L));
    verify(mockedDriver, times(1)).switchTo();
    verify(mockedSwitch, times(11)).frame("myname");
  }
}
