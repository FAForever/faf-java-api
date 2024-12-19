version = 3 -- Lua Version. Dont touch this
ScenarioInfo = {
    name = "Mirage",
    description = "Map By: Morax",
    preview = '',
    map_version = 10123,
    type = 'skirmish',
    starts = true,
    size = {256, 256},
    map = '/maps/mirage/mirage.scmap',
    save = '/maps/mirage/mirage_save.lua',
    script = '/maps/mirage/mirage_script.lua',
    norushradius = 40,
    Configurations = {
        ['standard'] = {
            teams = {
                {
                    name = 'FFA',
                    armies = {'ARMY_1', 'ARMY_2'}
                },
            },
            customprops = {
                ['ExtraArmies'] = STRING( 'ARMY_17 NEUTRAL_CIVILIAN' ),
            },
        },
    },
}
