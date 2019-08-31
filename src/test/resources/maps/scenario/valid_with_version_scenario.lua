version = 3 -- Lua Version. Dont touch this
ScenarioInfo = {
    name = "Mirage",
    description = "Map By: Morax",
    preview = '',
    map_version = 2,
    type = 'skirmish',
    starts = true,
    size = {256, 256},
    map = '/maps/mirage.v0002/mirage.scmap',
    save = '/maps/mirage.v0002/mirage_save.lua',
    script = '/maps/mirage.v0002/mirage_script.lua',
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
