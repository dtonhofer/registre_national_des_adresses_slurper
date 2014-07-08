registre_national_des_adresses_slurper
======================================

Code to read the "Registre National des Adresses" of Luxembourg into a memory structure

"Administration du Cadastre Luxembourg" makes availabe the database of addresses of Luxembourg on request.

See: http://www.act.public.lu/fr/parcelles-residences/registre-adresses/index.html

The data is a relational database dump, which one file per table.

The relationship are bit complex; see the documentation at:

http://www.act.public.lu/fr/publications/documents-techniques/specs_fichiers_adresses.pdf 

An illustration of the same:

![Initial Structure](https://raw.github.com/Q-LEAP/registre_national_des_adresses_slurper/master/images/Structure_BDD.png "Initial Structure")

The present program reads the files and simplifies the structure somewhat, creating a navigable in-memory structure which is simpler:

![Resulting Structure](https://raw.github.com/Q-LEAP/registre_national_des_adresses_slurper/master/images/Structure_Result.png "Resulting Structure")

As the contract under which the data is made available stipulates that you cannot distribute the "Registre National des Adresses", this project does not contain the data files themselves. You have to drop them into the ''eu.qleap.address.db.slurp.resources'' package, or create a new package, add the files to it and add a dummy class called ''Hook'' to that package which you then reference from the class ''eu.qleap.address.db.slurp.main.HookLocation''.

How to use this
---------------

To run this, you need a recent Java Virtual Machine and the Groovy jar. You also need the slf4j-api.jar for logging.

Alternatively, you can load the project directly into the Eclipse IDE, as the repository actually comes with a ''.project'' file.

Then run the ''eu.qleap.address.db.slurp.main.MainLoadNationalTree'' class. It constructs an in-memory structure called ''NationalTree'' 

To do
-----

So far, there is no code to dump this data structure into a database or XML, because the in-memory structure is used directly in further processing. This should be added for adequate utility.

