package net.frontlinesms.plugins.payment.service.safaricomke;

import org.creditsms.plugins.paymentview.data.repository.PaymentServiceSettingsDao;
import org.springframework.context.ApplicationContext;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.domain.PersistableSettings;
import net.frontlinesms.events.EventBus;
import net.frontlinesms.events.FrontlineEventNotification;
import net.frontlinesms.junit.BaseTestCase;
import net.frontlinesms.messaging.sms.events.SmsModemStatusNotification;
import net.frontlinesms.messaging.sms.modem.SmsModem;
import net.frontlinesms.messaging.sms.modem.SmsModemStatus;
import net.frontlinesms.plugins.payment.monitor.PaymentServiceMonitor;
import net.frontlinesms.plugins.payment.service.PaymentServiceStartRequest;

import static org.mockito.Mockito.*;

/** Tests for {@link ServiceMonitor} */
public class ServiceMonitorTest extends BaseTestCase {
	/** Object under test */
	private PaymentServiceMonitor m;
	private EventBus eventBus;
	private PaymentServiceSettingsDao settingsDao;
	
	@Override
	protected void setUp() throws Exception {
		m = new ServiceMonitor();

		eventBus = mock(EventBus.class);
		settingsDao = mock(PaymentServiceSettingsDao.class);
		
		FrontlineSMS f = mock(FrontlineSMS.class);
		when(f.getEventBus()).thenReturn(eventBus);
		
		ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBean("paymentServiceSettingsDao")).thenReturn(settingsDao);
		
		m.init(f, ctx);
	}
	
	/**
	 * GIVEN there is a payment service X configured for modem Y
	 * WHEN modem Y is connected
	 * THEN service X is started
	 */
	public void testServiceStartsWhenModemIsConnected() {
		// given
		SmsModem modem = mock(SmsModem.class);
		when(modem.getSerial()).thenReturn("ASDF");
		when(modem.getImsiNumber()).thenReturn("1234567890");
		final PersistableSettings settingsX = mock(PersistableSettings.class);
		when(settingsDao.getByProperties(
				AbstractPaymentService.PROPERTY_MODEM_SERIAL, "ASDF",
				AbstractPaymentService.PROPERTY_SIM_IMSI, "1234567890")).thenReturn(settingsX);
		
		// when
		m.notify(new SmsModemStatusNotification(modem, SmsModemStatus.CONNECTED));
		
		// then
		verify(eventBus).notifyObservers(new FrontlineEventNotification() {
			@Override
			public boolean equals(Object that) {
				if(!(that instanceof PaymentServiceStartRequest))
					return false;
				PaymentServiceStartRequest r = (PaymentServiceStartRequest) that;
				return r.getSettings() == settingsX;
			}
		});
	}
	
	/**
	 * GIVEN there is a payment service X configured for modem Y
	 * WHEN modem Y is connected
	 * THEN service X is started
	 */
	public void testServiceStartsWhenModemIsConnectedForUnknownImsi() {
		// given
		SmsModem modem = mock(SmsModem.class);
		when(modem.getSerial()).thenReturn("ASDF");
		when(modem.getImsiNumber()).thenReturn("1234567890");
		final PersistableSettings settingsX = mock(PersistableSettings.class);
		when(settingsDao.getByProperties(
				AbstractPaymentService.PROPERTY_MODEM_SERIAL, "ASDF",
				AbstractPaymentService.PROPERTY_SIM_IMSI, null)).thenReturn(settingsX);
		
		// when
		m.notify(new SmsModemStatusNotification(modem, SmsModemStatus.CONNECTED));
		
		// then
		verify(eventBus).notifyObservers(new FrontlineEventNotification() {
			@Override
			public boolean equals(Object that) {
				if(!(that instanceof PaymentServiceStartRequest))
					return false;
				PaymentServiceStartRequest r = (PaymentServiceStartRequest) that;
				return r.getSettings() == settingsX;
			}
		});
	}
}
