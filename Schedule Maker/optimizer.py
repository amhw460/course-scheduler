import requests
from bs4 import BeautifulSoup
import re
import pprint
from ortools.sat.python import cp_model
import json

courselist = []
professorblacklist = []

print("Please enter the list of courses you intend to take:")

while True:
    course = input("Course:").strip()

    if course != "done":
        courselist.append(course.upper())
    else:
        break
    prof = input("Are there any professors you want to avoid for this course?: ").strip()
    if prof != "none":
        professorblacklist.append(prof.upper())

print(courselist)
# print(professorblacklist)
TotalCourseSections = []
url = 'https://app.testudo.umd.edu/soc/search'
credits = {}
tcredits = 0
for course in courselist:
    params = {
        'courseId': course,
        'sectionId': '',
        'termId': '202508',
        '_openSectionsOnly': 'on',
        'creditCompare': '',
        'credits': '',
        'courseLevelFilter': 'ALL',
        'instructor': '',
        '_facetoface': 'on',
        '_blended': 'on',
        '_online': 'on',
        'courseStartCompare': '',
        'courseStartHour': '',
        'courseStartMin': '',
        'courseStartAM': '',
        'courseEndHour': '',
        'courseEndMin': '',
        'courseEndAM': '',
        'teachingCenter': 'ALL',
        '_classDay1': 'on',
        '_classDay2': 'on',
        '_classDay3': 'on',
        '_classDay4': 'on',
        '_classDay5': 'on'
    }

    response = requests.get(url, params=params)
    soup = BeautifulSoup(response.content, 'html.parser')
    credit = soup.find('span', class_= 'course-min-credits').text
    print(credit)
    credits[course] = credit
    tcredits += int(credit)
    sections = soup.find_all('div', class_='section delivery-f2f')
    if not sections:
        print("The class you indicated either doesn't exist or is not offered this semester.")
        continue
    # gets me list of sections for each class

    lsections = []
    for section in sections:
        new_section = {
            'section_id': section.find('span', class_='section-id').text.strip(),
            'instructor': [inst.text.strip().upper() for inst in section.find_all('span', class_='section-instructor')],
            'open': True if int(section.find('span', class_='open-seats-count').text.strip()) > 0 else False,
            'days': sum([re.findall(r'[A-Z][a-z]*', day.text.strip()) for day in
                         section.find_all('span', class_='section-days')], []),
            'times': [
                f"{(0 if start.text.strip().replace('am', '').replace('pm', '').split(':')[0] == '12' and 'am' in start.text.strip().lower() else (int(start.text.strip().replace('am', '').replace('pm', '').split(':')[0]) + (12 if 'pm' in start.text.strip().lower() and start.text.strip().replace('am', '').replace('pm', '').split(':')[0] != '12' else 0)))}:{start.text.strip().replace('am', '').replace('pm', '').split(':')[1] if ':' in start.text.strip() else '00'} - {(0 if end.text.strip().replace('am', '').replace('pm', '').split(':')[0] == '12' and 'am' in end.text.strip().lower() else (int(end.text.strip().replace('am', '').replace('pm', '').split(':')[0]) + (12 if 'pm' in end.text.strip().lower() and end.text.strip().replace('am', '').replace('pm', '').split(':')[0] != '12' else 0)))}:{end.text.strip().replace('am', '').replace('pm', '').split(':')[1] if ':' in end.text.strip() else '00'}"
                for start, end in zip(section.find_all('span', class_='class-start-time'),
                                      section.find_all('span', class_='class-end-time'))]
        }
        lsections.append(new_section)

        # print(section.find('span', class_='section-days').text.strip())
    TotalCourseSections.append({
        'Class': course,
        'Sections': lsections
    })

# pprint.pprint(TotalCourseSections)

timeslots = {}
map = {
    'M': 'Monday',
    'W': 'Wednesday',
    'F': 'Friday',
    'Tu': 'Tuesday',
    'Th': 'Thursday'
}
MWF = {
    'M': 'Monday',
    'W': 'Wednesday',
    'F': 'Friday'
}
tu_th = {
    'Tu': 'Tuesday',
    'Th': 'Thursday'
}
for course in TotalCourseSections:
    sections = course.get('Sections')
    timeslots[course.get('Class')] = []
    for section in reversed(sections):
        s = []
        if section.get('open') and all(prof not in professorblacklist for prof in section.get('instructor', [])):
            # now we need to check which day of the week ts is
            first = section.get('times')[0][:5].strip(), section.get('times')[0][-5:].strip()
            if any(day in section.get('days') for day in MWF) ^ any(day in section.get('days') for day in tu_th):
                for day in section.get('days'):
                    s.append((map[day], first[0], first[1]))
            else:
                second = section.get('times')[1][:5].strip(), section.get('times')[1][-5:].strip()
                if section.get('days')[0] in ['M', 'W', 'F']:
                    for day in section.get('days'):
                        if day in ['M', 'W', 'F']:
                            s.append((map[day], first[0], first[1]))
                        else:  # Tu, Th
                            s.append((map[day], second[0], second[1]))
                elif section.get('days')[-1] in ['M', 'W', 'F']:
                    for day in section.get('days'):
                        if day in ['Tu', 'Th']:
                            s.append((map[day], first[0], first[1]))
                        else:  # Tu, Th
                            s.append((map[day], second[0], second[1]))
        else:
            sections.remove(section)
        if s:
            timeslots[course.get('Class')].append(s)
    if not timeslots[course.get('Class')]:
        print(f"All desired instances of {course.get('Class')} are full. This class will be removed from the schedule builder now.")
        del timeslots[course.get('Class')]
# pprint.pprint(timeslots)
#
eventsnum = len(TotalCourseSections)
#


model = cp_model.CpModel()
# #
decision_vars = {}

# ok
# so for each timeslot for each class, we number this and tie it to a boolean

for classes, times in timeslots.items():
    for i, slot in enumerate(times):
        decision_vars[(classes, i)] = model.NewBoolVar(f'{classes}_slot_{i}')
#
# pprint.pprint(decision_vars)

for classes, times in timeslots.items():
    model.Add(sum(decision_vars[(classes, i)] for i in range(len(times))) == 1)


# now we need to add a second constraint that they cant overlap.
# check each day sequentially
#

def time_to_minutes(str):
    hour = int(str[0:str.find(':')])
    minute = int(str[-2:])
    return hour * 60 + minute


def times_overlap_same_day(time1, time2):
    day1, start1, end1 = time1
    day2, start2, end2 = time2

    if day1 != day2:
        return False

    start1min = time_to_minutes(start1)
    end1min = time_to_minutes(end1)
    start2min = time_to_minutes(start2)
    end2min = time_to_minutes(end2)
    return not (end1min <= start2min or end2min <= start1min)


def timeslots_overlap(section1, section2):
    # print(f"Checking course1: {course1}, course2: {course2}")
    # print(f"section1 type: {type(section1)}, section2 type: {type(section2)}")
    # print(f"section1: {section1}")
    # print(f"section2: {section2}")

    if isinstance(section1, tuple):
        section1 = [section1]  # Convert single tuple to list
    if isinstance(section2, tuple):
        section2 = [section2]  # Convert single tuple to list
    for time1 in section1:
        for time2 in section2:
            if times_overlap_same_day(time1, time2):
                # print(f"  Comparing time1[{i}]: {time1} (len={len(time1)})")
                # print(f"  Comparing time2[{j}]: {time2} (len={len(time2)})")
                return True
    return False


for course1, sections1 in timeslots.items():
    for course2, sections2 in timeslots.items():
        if course1 >= course2:
            continue
        for i, section1 in enumerate(sections1):  # Now using sections1
            for j, section2 in enumerate(sections2):  # Now using sections2
                if timeslots_overlap(section1, section2):
                    model.Add(decision_vars[(course1, i)] + decision_vars[(course2, j)] <= 1)


def get_instructor_and_section(data, class_name, section_index):
    for c in data:
        if c['Class'] == class_name:
            if section_index < len(c['Sections']):
                sct = c['Sections'][section_index]
                return [sct['instructor'], sct['section_id']]
    return None, None

# ok now time to minimize!
days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday']
early = 1440
late = 0
for section in timeslots.values():
    for s in section:
        for day, start, end in s:
            early = min(early, time_to_minutes(start))
            late = max(late, time_to_minutes(end))


daily_earliest = {}
daily_latest = {}
daily_uptime = {}
daily_has_classes = {}

for day in days:
    daily_earliest[day] = model.NewIntVar(early, late, f'{day}_earliest')
    daily_latest[day] = model.NewIntVar(early, late, f'{day}_latest')  # Fixed typo
    daily_uptime[day] = model.NewIntVar(0, late - early, f'{day}_uptime')
    daily_has_classes[day] = model.NewBoolVar(f'{day}_has_classes')

for day in days:
    d = []
    for course, sections in timeslots.items():
        for i, section in enumerate(sections):
            for mday, start, end in section:
                if day == mday:
                    d.append((course, i, time_to_minutes(start), time_to_minutes(end)))

    if d:
        # Check if any classes are scheduled on this day
        temp = [decision_vars[(course, i)] for course, i, start_min, end_min in d]
        model.AddBoolOr(temp).OnlyEnforceIf(daily_has_classes[day])

        # For each possible class time on this day, constrain earliest/latest
        for course, i, start_min, end_min in d:
            model.Add(daily_earliest[day] <= start_min).OnlyEnforceIf(decision_vars[(course, i)])
            model.Add(daily_latest[day] >= end_min).OnlyEnforceIf(decision_vars[(course, i)])

        # When no classes, set reasonable defaults
        model.Add(daily_earliest[day] == early).OnlyEnforceIf(daily_has_classes[day].Not())
        model.Add(daily_latest[day] == early).OnlyEnforceIf(daily_has_classes[day].Not())
    else:
        # No possible classes on this day
        model.Add(daily_has_classes[day] == 0)
        model.Add(daily_earliest[day] == early)
        model.Add(daily_latest[day] == early)

        # Calculate uptime
    model.Add(daily_uptime[day] == daily_latest[day] - daily_earliest[day]).OnlyEnforceIf(daily_has_classes[day])
    model.Add(daily_uptime[day] == 0).OnlyEnforceIf(daily_has_classes[day].Not())

total_uptime = model.NewIntVar(0, 5 * (late - early), 'Total daily uptime')
model.Add(total_uptime == sum(daily_uptime[day] for day in days))
model.Minimize(total_uptime)

solver = cp_model.CpSolver()
status = solver.Solve(model)
final = []
if status == cp_model.OPTIMAL:
    print("Optimal schedule:")

    for course, sections in timeslots.items():
        for i, section in enumerate(sections):
            if solver.Value(decision_vars[(course, i)]) == 1:
                t = {}
                print(f"\n{course} -> Section {i}:")
                t["Course"] = course
                sct = get_instructor_and_section(TotalCourseSections, course, i)[1]
                instruct = get_instructor_and_section(TotalCourseSections, course, i)[0]
                t["ID"] = sct
                t["Instructor(s)"] = instruct
                print(f"Credits: {credits[course]}")
                t["Times"] = [f"{day}: {start} - {end}" for day, start, end in section]
                for day, start, end in section:
                    print(f"  {day}: {start} - {end}")
                print(f'{sct} with {instruct}')
                final.append(t)

print(f"Total credit hours in this semester: {tcredits}")

with open('output.json', 'w') as f:
    json.dump(final, f, indent = 4)
