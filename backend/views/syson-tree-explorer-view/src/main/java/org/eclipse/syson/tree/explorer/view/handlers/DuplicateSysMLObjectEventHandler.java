/*******************************************************************************
 * Copyright (c) 2025 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.syson.tree.explorer.view.handlers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.IEditingContextEventHandler;
import org.eclipse.sirius.components.collaborative.api.Monitoring;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.web.application.views.explorer.services.DuplicateObjectEventHandler;
import org.eclipse.sirius.web.application.views.explorer.services.api.DuplicationSettings;
import org.eclipse.sirius.web.domain.services.api.IMessageService;
import org.eclipse.syson.services.api.ISysMLDuplicateElementService;
import org.eclipse.syson.services.api.ISysMLDuplicateElementService.DuplicateElementStatus;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.tree.explorer.view.controllers.EditingContextMembershipTypeNamesDataFetcher;
import org.eclipse.syson.tree.explorer.view.dto.DuplicateSysMLObjectInput;
import org.eclipse.syson.tree.explorer.view.dto.DuplicateSysMLObjectSuccessPayload;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;

/**
 * {@link IEditingContextEventHandler} for duplicating an object in SysML.
 *
 * @author flatombe
 * @see DuplicateObjectEventHandler
 * @see DuplicateSysMLObjectInput
 * @see DuplicateSysMLObjectSuccessPayload
 * @see EditingContextMembershipTypeNamesDataFetcher
 */
@Service
public class DuplicateSysMLObjectEventHandler implements IEditingContextEventHandler {

    private static final String DUPLICATED_OBJECT = "duplicatedObject";

    private static final String DUPLICATION_DESTINATION = "duplicationDestination";

    private static final String CREATED_MEMBERSHIP = "createdMembership";

    private final ISysMLDuplicateElementService sysMLDuplicateElementService;

    private final IObjectSearchService objectSearchService;

    private final IMessageService messageService;

    private final Counter counter;

    public DuplicateSysMLObjectEventHandler(final ISysMLDuplicateElementService sysMLDuplicateElementService, final IObjectSearchService objectSearchService, final IMessageService messageService,
            final MeterRegistry meterRegistry) {
        this.sysMLDuplicateElementService = Objects.requireNonNull(sysMLDuplicateElementService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.messageService = Objects.requireNonNull(messageService);
        this.counter = Counter.builder(Monitoring.EVENT_HANDLER).tag(Monitoring.NAME, this.getClass().getSimpleName()).register(meterRegistry);
    }

    @Override
    public boolean canHandle(final IEditingContext editingContext, final IInput input) {
        return input instanceof DuplicateSysMLObjectInput;
    }

    @Override
    public void handle(final One<IPayload> payloadSink, final Many<ChangeDescription> changeDescriptionSink, final IEditingContext editingContext, final IInput input) {
        this.counter.increment();

        final List<Message> messages = List.of(new Message(this.messageService.invalidInput(input.getClass().getSimpleName(), DuplicateSysMLObjectInput.class.getSimpleName()), MessageLevel.ERROR));
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, editingContext.getId(), input);
        IPayload payload = new ErrorPayload(input.id(), messages);

        if (input instanceof DuplicateSysMLObjectInput duplicateSysMLObjectInput) {
            final DuplicationSettings settings = new DuplicationSettings(duplicateSysMLObjectInput.duplicateContents(), duplicateSysMLObjectInput.copyOutgoingReferences(),
                    duplicateSysMLObjectInput.updateIncomingReferences());
            final IStatus duplicationResult = this.duplicateSysMLObject(editingContext, duplicateSysMLObjectInput.objectId(), duplicateSysMLObjectInput.ownerId(),
                    duplicateSysMLObjectInput.membershipTypeName(),
                    settings);

            if (duplicationResult instanceof Success success) {
                payload = new DuplicateSysMLObjectSuccessPayload(input.id(), success.getParameters().get(DUPLICATED_OBJECT), success.getParameters().get(DUPLICATION_DESTINATION),
                        success.getParameters().get(CREATED_MEMBERSHIP), success.getMessages());
                changeDescription = new ChangeDescription(success.getChangeKind(), editingContext.getId(), input);
            } else if (duplicationResult instanceof Failure failure) {
                payload = new ErrorPayload(input.id(), failure.getMessages());
            }
        }

        payloadSink.tryEmitValue(payload);
        changeDescriptionSink.tryEmitNext(changeDescription);
    }

    private IStatus duplicateSysMLObject(final IEditingContext editingContext, final String objectId, final String ownerId, final String membershipTypeName,
            final DuplicationSettings settings) {
        final Optional<Element> maybeObject = this.objectSearchService.getObject(editingContext, objectId)
                .filter(Element.class::isInstance)
                .map(Element.class::cast);
        final Optional<Element> maybeOwner = this.objectSearchService.getObject(editingContext, ownerId)
                .filter(Element.class::isInstance)
                .map(Element.class::cast);

        final IStatus result;
        if (maybeObject.isEmpty()) {
            result = new Failure(this.messageService.objectDoesNotExist(objectId));
        } else if (maybeOwner.isEmpty()) {
            result = new Failure(this.messageService.objectDoesNotExist(ownerId));
        } else {
            result = this.duplicateSysMLObject(editingContext, maybeObject.get(), maybeOwner.get(), membershipTypeName, settings);
        }

        return result;
    }

    private IStatus duplicateSysMLObject(final IEditingContext editingContext, final Element object, final Element owner, final String membershipTypeName, final DuplicationSettings settings) {
        final DuplicateElementStatus status = sysMLDuplicateElementService.duplicateElement(editingContext, object, owner, membershipTypeName, settings.duplicateContent(),
                settings.copyOutgoingReferences(), settings.updateIncomingReferences());

        if (status.isSuccess()) {
            return new Success(ChangeKind.SEMANTIC_CHANGE,
                    Map.of(DUPLICATED_OBJECT, status.duplicatedElement(), DUPLICATION_DESTINATION, status.duplicationDestination(), CREATED_MEMBERSHIP, status.createdMembership()));
        } else {
            return new Failure(this.messageService.objectDuplicationFailed());
        }
    }
}
