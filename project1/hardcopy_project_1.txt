#Author: THomas Haddy 9/19/2018
#COMS 363 Project 1

#Create the tables

#Item 1
create table Person (
	Name char (20),
	ID char (9) not null,
	Address char (30),
	DOB date,
	Primary key (ID)
);

#Item 2
create table Instructor (
	InstructorID char (9) not null,
    Rank char (12),
    Salary int,
    Primary key (InstructorID),
    Foreign key (InstructorID) references Person(ID)
);

#Item 3
create table Student (
	StudentID char (9) not null,
    Classification char (10),
    GPA double,
    MentorID char (9),
    CreditHours int,
    Primary key (StudentID),
    Foreign key (MentorID) references Instructor(InstructorID)
);

#Item 4
create table Course (
	CourseCode char (6) not null,
    CourseName char (50),
    PreReq char (6), #'None' gets entered for no prereqs
    Primary key (CourseName, CourseCode)
);

#Item 5
create table Offering (
	CourseCode char (6) not null,
    SectionNo int not null,
    InstructorID char (9) not null,
    Primary key (CourseCode, SectionNo),
    Foreign key (InstructorID) references Instructor(InstructorID)
);

#Item 6
create table Enrollment (
	CourseCode char(6) not null, 
	SectionNo int not null, 
	StudentID char(9) not null references Student, 
	Grade char(4) not null, 
	Primary key (CourseCode, StudentID), 
	Foreign key (CourseCode, SectionNo) references Offering(CourseCode, SectionNo)
);

#Loading tables with data

#Item 7
load xml local infile 'C:/Users/Thomas/Documents/coms_363/project1/Person.xml'
into table Person 
rows identified by '<Person>';

#Item 8
load xml local infile 'C:/Users/Thomas/Documents/coms_363/project1/Instructor.xml'
into table Instructor 
rows identified by '<Instructor>';

#Item 9
load xml local infile 'C:/Users/Thomas/Documents/coms_363/project1/Student.xml'
into table Student 
rows identified by '<Student>';

#Item 10
load xml local infile 'C:/Users/Thomas/Documents/coms_363/project1/Course.xml'
into table Course
rows identified by '<Course>';

#Item 11
load xml local infile 'C:/Users/Thomas/Documents/coms_363/project1/Offering.xml'
into table Offering
rows identified by '<Offering>';

#Item 12
load xml local infile 'C:/Users/Thomas/Documents/coms_363/project1/Enrollment.xml'
into table Enrollment 
rows identified by '<Enrollment>';

#Query problems

#Item 13
select S.StudentID, S.MentorID
from Student S
where GPA > 3.8 and (S.Classification = 'Junior' or S.Classification = 'Senior');

#Item 14
select distinct O.CourseCode, O.SectionNo
from Offering O
inner join Enrollment E on O.CourseCode = E.CourseCode
inner join Student S on E.StudentID = S.StudentID and S.Classification = 'Sophomore';

#Item 15
select distinct P.Name, I.Salary
from Person P
inner join Instructor I on P.ID = I.InstructorID
inner join Student S on I.InstructorID = S.MentorID and S.Classification = 'Freshman';
    
#Item 16
select sum(I.Salary)
from Instructor I
where I.InstructorID not in (select O.InstructorID from Offering O);
                    
#Item 17
select P.Name, P.DOB
from Person P
inner join Student S on P.ID = S.StudentID
where year(P.DOB) = 1976;

#Item 18
select P.Name, I.Rank
from Person P
inner join Instructor I on P.ID = I.InstructorID
where I.InstructorID not in (select O.InstructorID from Offering O) and I.InstructorID not in (select S.MentorID from Student S);

#Item 19
select S.StudentID, P.Name, max(P.DOB)
from Person P
inner join Student S on P.ID = S.StudentID;

#Item 20
select P.ID, P.DOB, P.Name
from Person P
where P.ID not in (select S.StudentID from Student S) and P.ID not in (select I.InstructorID from Instructor I);
#where

#Item 21
select P.name, count(S.MentorID)
from Instructor I
left join Person P on I.InstructorID = P.ID
left join Student S on I.InstructorID = S.MentorID
group by I.InstructorID;

#Item 22
select S.Classification, count(S.StudentID), avg(S.GPA)
from Student S
group by S.Classification
order by S.Classification; #It will be ordered by: Freshman, Junior, Senior, Sophomore

#Item 23
select E.CourseCode, count(E.StudentID)
from Enrollment E
group by E.CourseCode
order by count(E.StudentID) asc;

#Item 24
select S.StudentID, S.MentorID
from Student S
join Enrollment E on S.StudentID = E.StudentID
join Offering O on E.CourseCode = O.CourseCode
where O.InstructorID = S.MentorID
	  and O.CourseCode = E.CourseCode
	  and E.StudentID = S.StudentID;

#Item 25
select S.StudentID, P.Name, S.CreditHours
from Student S
left join Person P on S.StudentID = P.ID
where S.Classification = 'Freshman' and P.DOB >= '1976';

#Item 26
insert into Person (Name, ID, Address, DOB)
values ('Briggs Jason', '480293439', '215 North Hyland Avenue', '1975-01-15');

insert into Student (StudentID, Classification, GPA, MentorID, CreditHours)
values ('480293439', 'Junior', 3.48, '201586985', 75);

insert into Enrollment (CourseCode, SectionNo, StudentID, Grade)
values ('CS311', 2, '480293439', 'A');

insert into Enrollment (CourseCode, SectionNo, StudentID, Grade)
values ('CS330', 1, '480293439', 'A-');

select *
from Person P
where P.Name = 'Briggs Jason';

select *
from Student S
where S.StudentID = '480293439';

select *
from Enrollment E
where E.StudentID = '480293439';

#Item 27
delete from Enrollment
where Enrollment.StudentID in (select S.StudentID from Student S where GPA < 0.5);

delete from Student
where Student.GPA < 0.5;

select *
from Student S
where S.GPA < 0.5;

#Item 28
select P.Name, I.Salary
from Instructor I, Person P
where I.InstructorID = P.ID and P.Name = 'Ricky Ponting';

update Instructor
inner join Person on Instructor.InstructorID = Person.ID
Set Instructor.Salary = CASE
	WHEN 
    (
		select count(Student.MentorID) as total_ricky
		from Person
		left join Student on Person.ID = MentorID
		where(Person.Name = 'Ricky Ponting') and Student.GPA > 3.0
	)
	>= 5 and Person.Name = 'Ricky Ponting' then Instructor.Salary * 1.10
    ELSE Instructor.Salary
END;

select P.Name, I.Salary
from Instructor I, Person P
where I.InstructorID = P.ID
and P.Name = 'Ricky Ponting';

#Item 29
insert into Person (Name, ID, Address, DOB)
values ('Trevor Horns', '000957303', '23 Canberra Street', '1964-11-23');

select *
from Person P
where P.Name = 'Trevor Horns';

#Item 30
delete from Student
where StudentID in (select P.ID From Person P where P.name = 'Jan Austin');

delete from Person
where Name = 'Jan Austin';

select *
from Person P
where P.Name = 'Jan Austin';