package com.welcom.modules.event;

import com.welcom.modules.account.Account;
import com.welcom.modules.account.CurrentAccount;
import com.welcom.modules.company.Company;
import com.welcom.modules.event.form.EventForm;
import com.welcom.modules.company.CompanyRepository;
import com.welcom.modules.company.CompanyService;
import com.welcom.modules.event.validator.EventFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/company/{path}")
@RequiredArgsConstructor
public class EventController {

    private final CompanyService companyService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventFormValidator eventFormValidator;
    private final CompanyRepository companyRepository;
    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventFormValidator);
    }


    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Company company = companyService.getCompanyToUpdateStatus(account, path);
        model.addAttribute(account);
        model.addAttribute(company);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentAccount Account account, @PathVariable String path, @Valid EventForm eventForm, Errors errors, Model model) {
        Company company = companyService.getCompanyToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(company);
            return "event/form";
        }
        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), company, account);
        return "redirect:/company/" + company.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id, Model model) {
        model.addAttribute(account);
        model.addAttribute(eventRepository.findById(id).orElseThrow());
        model.addAttribute(companyService.getCompany(path));
        return "event/view";
    }

    @GetMapping("/events")
    public String viewCompanyEvents(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Company company = companyService.getCompany(path);
        model.addAttribute(account);
        model.addAttribute(company);
        List<Event> events = eventRepository.findByCompanyOrderByStartDateTime(company);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        // events 에 있는 값을 isBefor로 현재시간이 지난것은 oldEvents 에 넣어주고 아닌건 newEvents에 넣어준다.
        events.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(e);
            } else {
                newEvents.add(e);
            }
        });
        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);
        return "company/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event, Model model) {
        Company company = companyService.getCompanyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(company);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "/event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id, @Valid EventForm eventForm, Errors errors, Model model) {
        Company company = companyService.getCompanyToUpdate(account, path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventForm.setEventType(event.getEventType());
        eventFormValidator.validateUpdateForm(event, eventForm, errors);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(company);
            model.addAttribute(event);
            return "event/update-form";
        }
        eventService.updateEvent(event, eventForm);
        return "redirect:/company/" + company.getEncodedPath() + "/events/" + event.getId();
    }

    @DeleteMapping("/events/{id}")
    public String cancelEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Company company = companyService.getCompanyToUpdateStatus(account, path);
        eventService.deleteEvent(eventRepository.findById(id).orElseThrow());
        return "redirect:/company/" + company.getEncodedPath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Company company = companyService.getCompanyToEnroll(path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventService.newEnrollment(event, account);
        return "redirect:/company/" + company.getEncodedPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Company company = companyService.getCompanyToEnroll(path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventService.cancelEnrollment(event, account);
        return "redirect:/company/" + company.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount Account account, @PathVariable String path,@PathVariable Long eventId, @PathVariable Long enrollmentId) {
        Company company = companyService.getCompanyToUpdate(account, path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.acceptEnrollment(event,enrollment);
        return "redirect:/company/" + company.getEncodedPath() + "/events/" + event.getId();
    }
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        Company company = companyService.getCompanyToUpdate(account, path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.rejectEnrollment(event,enrollment);
        return "redirect:/company/" + company.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        Company company = companyService.getCompanyToUpdate(account, path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.checkInEnrollment(enrollment);
        return "redirect:/company/" + company.getEncodedPath() + "/events/" + event.getId();
    }
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentAccount Account account,@PathVariable String path, @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        Company company = companyService.getCompanyToUpdate(account, path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/company/" + company.getEncodedPath() + "/events/" + event.getId();
    }

}
