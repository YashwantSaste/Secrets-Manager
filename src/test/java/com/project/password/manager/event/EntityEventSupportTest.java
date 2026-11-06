package com.project.password.manager.event;

import java.util.List;

import com.project.password.manager.event.listener.IEventListener;
import com.project.password.manager.model.IEntity;
import com.project.password.manager.util.ModelObjectMapperFactory;

import junit.framework.TestCase;

public class EntityEventSupportTest extends TestCase {

	public void testPublishUpdatedEmitsDetectedFieldChanges() {
		RecordingEventListener listener = new RecordingEventListener();
		EntityEventSupport eventSupport = new EntityEventSupport(
				new EntitySnapshotter(ModelObjectMapperFactory.create()),
				new EntityEventFactory(new EntityChangeDetector(ModelObjectMapperFactory.create())),
				new EventDispatcher(List.of(listener)));

		TestEntity before = new TestEntity("alpha", "secret-1");
		TestEntity after = eventSupport.snapshot(before);
		after.setValue("secret-2");

		eventSupport.publishUpdated(before, after);

		assertNotNull(listener.getLastEvent());
		assertEquals(EventType.UPDATE, listener.getLastEvent().type());
		assertEquals("TestEntity", listener.getLastEvent().entityType());
		assertEquals("alpha", listener.getLastEvent().entityId());
		assertEquals(1, listener.getLastEvent().changes().size());
		assertEquals("value", listener.getLastEvent().changes().get(0).getFieldName());
	}
}

class RecordingEventListener implements IEventListener {

	private IEvent lastEvent;

	@Override
	public boolean supports(IEvent event) {
		return true;
	}

	@Override
	public void onEvent(IEvent event) {
		lastEvent = event;
	}

	public IEvent getLastEvent() {
		return lastEvent;
	}
}

class TestEntity implements IEntity {

	private String id;
	private String value;

	TestEntity() {
	}

	TestEntity(String id, String value) {
		this.id = id;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public Class<?> entityClassType() {
		return TestEntity.class;
	}
}