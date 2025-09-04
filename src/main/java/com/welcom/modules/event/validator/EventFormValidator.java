package com.welcom.modules.event.validator;

import com.welcom.modules.event.Event;
import com.welcom.modules.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm)target;

        if (isNotValidEndEnrollmentDateTime(eventForm)) {
            errors.rejectValue("endEnrollmentDateTime", "wrong.dateTime","등록 마감 일시를 정확히 입력하세요.");
        }
        if (isNotValidEndDateTime(eventForm)) {
            errors.rejectValue("endDateTime", "wrong.dateTime", "공고 종료 일시를 정확히 입력하세요.");
        }
        if (isNotValidStartDateTime(eventForm)) {
            errors.rejectValue("startDateTime", "wrong.dateTime", "공고 시작 일시를 정확히 입력하세요.");
        }

    }

    private boolean isNotValidStartDateTime(EventForm eventForm) {
        return eventForm.getStartDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    private boolean isNotValidEndDateTime(EventForm eventForm) {
        LocalDateTime endDateTime = eventForm.getEndDateTime();
        return eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime()) || endDateTime.isBefore(eventForm.getEndEnrollmentDateTime());
    }

    private boolean isNotValidEndEnrollmentDateTime(EventForm eventForm) {
        return eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }

    public void validateUpdateForm(Event event, EventForm eventForm, Errors errors) {
        // 모집인원은 확정된 참가 신청수 보다 많아야 한다.
        if (eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()) {
            errors.rejectValue("limitOfEnrollments","wrong.value","확인된 참가 신청수 보다 모집 인원수가 더 많아야 합니다.");
        }

    }
}
