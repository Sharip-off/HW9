package ua.kiev.prog;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@Controller
public class MyController {
    static final int DEFAULT_GROUP_ID = -1;
    static final int ITEMS_PER_PAGE = 6;

    private final ContactService contactService;

    public MyController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false, defaultValue = "0") Integer page) {
        if (page < 0) page = 0;

        List<Contact> contacts = contactService
                .findAll(PageRequest.of(page, ITEMS_PER_PAGE, Sort.Direction.DESC, "id"));

        model.addAttribute("groups", contactService.findGroups());
        model.addAttribute("contacts", contacts);
        model.addAttribute("allPages", getPageCount());

        return "index";
    }

    @GetMapping("/reset")
    public String reset() {
        contactService.reset();
        return "redirect:/";
    }


    @GetMapping("/contact_add_page")
    public String contactAddPage(Model model) {
        model.addAttribute("groups", contactService.findGroups());
        return "contact_add_page";
    }

    @GetMapping("/group_add_page")
    public String groupAddPage() {
        return "group_add_page";
    }

    @GetMapping("/group/{id}")
    public String listGroup(
            @PathVariable(value = "id") long groupId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            Model model) {
        Group group = (groupId != DEFAULT_GROUP_ID) ? contactService.findGroup(groupId) : null;
        if (page < 0) page = 0;

        List<Contact> contacts = contactService
                .findByGroup(group, PageRequest.of(page, ITEMS_PER_PAGE, Sort.Direction.DESC, "id"));

        model.addAttribute("groups", contactService.findGroups());
        model.addAttribute("contacts", contacts);
        model.addAttribute("byGroupPages", getPageCount(group));
        model.addAttribute("groupId", groupId);

        return "index";
    }

    @RequestMapping(value = "/search")
    public String search(@RequestParam String pattern,
                         @RequestParam(required = false, defaultValue = "0") Integer page,
                         Model model) {
        if (page < 0) page = 0;
        model.addAttribute("groups", contactService.findGroups());
        List<Contact> contacts = contactService.findByPattern(pattern,
                PageRequest.of(page, ITEMS_PER_PAGE, Sort.Direction.DESC, "id"));
        model.addAttribute("contacts", contacts);
        model.addAttribute("pattern", pattern);
        model.addAttribute("byPattern", getPageCount(pattern));

        return "index";
    }

    @PostMapping(value = "/contact/delete")
    public ResponseEntity<Void> delete(
            @RequestParam(value = "toDelete[]", required = false) long[] toDelete) {
        if (toDelete != null && toDelete.length > 0)
            contactService.deleteContacts(toDelete);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/contact/add")
    public String contactAdd(@RequestParam(value = "group") long groupId,
                             @RequestParam String name,
                             @RequestParam String surname,
                             @RequestParam String phone,
                             @RequestParam String email) {
        Group group = (groupId != DEFAULT_GROUP_ID) ? contactService.findGroup(groupId) : null;

        Contact contact = new Contact(group, name, surname, phone, email);
        contactService.addContact(contact);

        return "redirect:/";
    }

    @PostMapping(value = "/group/add")
    public String groupAdd(@RequestParam String name) {
        contactService.addGroup(new Group(name));
        return "redirect:/";
    }

    @GetMapping("/saveCSV")
    public void downloadCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"contacts.csv\"");

        List<Contact> contacts = contactService.findContacts();

        PrintWriter writer = response.getWriter();
        writer.println("Name,Surname,Phone,Email,Group");

        for (Contact contact : contacts) {
            String groupName = (contact.getGroup() != null) ? contact.getGroup().getName() : "Default";
            writer.println(String.format("%s,%s,%s,%s,%s",
                    contact.getName(),
                    contact.getSurname(),
                    contact.getPhone(),
                    contact.getEmail(),
                    groupName));
        }

        writer.close();
    }

    @GetMapping("/upload")
    public String upload(Model model) {
        return "uploadCSV";
    }

    @PostMapping(value = "/uploadCSV")
    public String uploadCSV(@RequestParam("CSVfile") MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            for (; (line = br.readLine()) != null; ) {
                String[] fields = line.split(",");
                if (fields.length >= 5) {
                    String name = fields[0];
                    String surname = fields[1];
                    String phone = fields[2];
                    String email = fields[3];
                    String groupName = fields[4];

                    Group group = contactService.findGroupByName(groupName);
                    if (group == null) {
                        group = new Group(groupName);
                        contactService.addGroup(group);
                    }

                    Contact contact = new Contact(group, name, surname, phone, email);
                    contactService.addContact(contact);
                }
            }
            return "redirect:/";
        }
    }


    private long getPageCount() {
        long totalCount = contactService.count();
        return (totalCount / ITEMS_PER_PAGE) + ((totalCount % ITEMS_PER_PAGE > 0) ? 1 : 0);
    }

    private long getPageCount(Group group) {
        long totalCount = contactService.countByGroup(group);
        return (totalCount / ITEMS_PER_PAGE) + ((totalCount % ITEMS_PER_PAGE > 0) ? 1 : 0);
    }

    private long getPageCount(String pattern) {
        long totalCount = contactService.findByPattern(pattern, null).size();
        return (totalCount / ITEMS_PER_PAGE) + ((totalCount % ITEMS_PER_PAGE > 0) ? 1 : 0);
    }
}
