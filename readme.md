# A project to collect all the utility classes that can be shared between projects.

## Features

- [x] QueryDsl Parser to write filters in natural language;
- [x] Page implementation;
- [x] DynamicDto builder to handle JSON responses;
- [x] Global repository to handle CRUD operations;
- [x] Something else!

## QueryDsl filter parser
The entities that will be created by querydsl must implement the Filterable interface.

Querydsl will generate classes of type Q. In this way, after creating them, it is necessary to override the gitFilters method with the expressions that will be possible to use this entity.

In the example below, only the property id will can be used in filter;

```
public class User implements Filterable {
    private Long id;
    private String username;
    
    @Override
    @JsonIgnore
    public List<ComparableExpressionBase> getFilters() {
        return Arrays.asList(
            QUser.user.id
        );
    }
}
```

After that, in the controller that will receive the request, it will be necessary to call the method responsible for the parser of the filter passed as a parameter in the URL.

The filter can be written like this: "id = 1 or id in (1)"

```
@GetMapping
public List<User> findAll(@RequestParam(value = "filter", required = false) String filter) {
    final BooleanBuilder finalFilter = NaturalQueryParser.parse(filter, User.class);

    return userService.findAll(finalFilter); //the service must be something to handle a filter
}
```

## DynamicDto builder
All Entity classes needs to implement a DatabaseEntity<{ Type of your Entity id (Long, Integer or UUID) }>.

After that, build your json this way (Simples ResponseEntity Controller):
```
public ResponseEntity<DynamicDto> findAll() {
  Employee employee = new Employee("João Henrique", 12345678910, "Male", "SC");
  
  DynamicDto dto = 
    DynamicDto.of(employee)
      .with("name", employee.getName())
      .with("cpf", employee.getCpf())
      .with("gender", employee.getGender())
      .with("state", employee.getState());
      
  return ResponseEntity.ok(dto);
}
```

## 🚀 Please, enjoy!

⌨️ with ❤️ by [João Henrique](https://github.com/jouiwnl) 😊
